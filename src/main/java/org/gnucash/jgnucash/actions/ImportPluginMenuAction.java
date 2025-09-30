/**
 * ImportPluginMenuAction.java
 * Created on 06.11.2005
 * (c) 2005 by "Wolschon Softwaredesign und Beratung".
 *
 *  Permission is granted to use, modify, publish and sub-license this code
 *  as specified in the contract. If nothing else is specified these rights
 *  are given non-exclusively with no restrictions solely to the contractor(s).
 *  If no specified otherwise I reserve the right to use, modify, publish and
 *  sub-license this code to other parties myself.
 *
 * Otherwise, this code is made available under GPLv3 or later.
 *
 * -----------------------------------------------------------
 * major Changes:
 *  06.11.2005 - initial version, factored out of jGnucash
 * ...
 *
 */
package org.gnucash.jgnucash.actions;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;

import org.java.plugin.registry.Extension;

import org.gnucash.api.write.GnuCashWritableAccount;
import org.gnucash.api.write.GnuCashWritableFile;
import org.gnucash.jgnucash.JGnuCash;
import org.gnucash.jgnucash.plugin.ImporterPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The action-listeners we use for the ImportMenu.
 */
public final class ImportPluginMenuAction implements ActionListener {
    /**
     * Our logger for debug- and error-output.
     */
    static final Logger LOGGER = LoggerFactory.getLogger(ImportPluginMenuAction.class);

    /**
     * Our JGnucash.java.
     * @see ImportPluginMenuAction
     */
    private final JGnuCash myJGnucashEditor;
    /**
     * @param aPlugin The import-plugin.
     * @param aPluginName The name of the plugin
     * @param aGnucash TODO
     */
    public ImportPluginMenuAction(final JGnuCash aGnucash, final Extension aPlugin, final String aPluginName) {
        super();
        myJGnucashEditor = aGnucash;
        ext = aPlugin;
        pluginName = aPluginName;
    }
    /**
     * The import-plugin.
     */
    private final Extension ext;

    /**
     * The name of the plugin.
     */
    private final String pluginName;

    @Override
    public void actionPerformed(final ActionEvent e) {
        try {
            GnuCashWritableFile wModel = myJGnucashEditor.getWritableModel();
            if (wModel == null) {
                JOptionPane.showMessageDialog(myJGnucashEditor, "No open file.",
                        "Please open a gnucash-file first!",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Activate plug-in that declares extension.
            myJGnucashEditor.getPluginManager().activatePlugin(ext.getDeclaringPluginDescriptor().getId());
            // Get plug-in class loader.
            ClassLoader classLoader = myJGnucashEditor.getPluginManager().getPluginClassLoader(
                                      ext.getDeclaringPluginDescriptor());
            // Load Tool class.
            Class toolCls = classLoader.loadClass(
                    ext.getParameter("class").valueAsString());
            // Create Tool instance.
            Object o = toolCls.newInstance();
            if (!(o instanceof ImporterPlugin)) {
                LOGGER.error("Plugin '" + pluginName + "' does not implement ImporterPlugin-interface.");
                JOptionPane.showMessageDialog(myJGnucashEditor, "Error",
                        "Plugin '" + pluginName + "' does not implement ImporterPlugin-interface.",
                        JOptionPane.ERROR_MESSAGE);
                return;

            }
            ImporterPlugin importer = (ImporterPlugin) o;
            try {
                myJGnucashEditor.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                GnuCashWritableAccount selectedAccount = (GnuCashWritableAccount) myJGnucashEditor.getSelectedAccount();
                String message = importer.runImport(wModel, selectedAccount);
                if (message != null && message.length() > 0) {
                    JOptionPane.showMessageDialog(myJGnucashEditor, "Import OK",
                            "The import was a success:\n" + message,
                            JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception e1) {
                LOGGER.error("Import via Plugin '" + pluginName + "' failed.", e1);
                JOptionPane.showMessageDialog(myJGnucashEditor, "Error",
                        "Import via Plugin '" + pluginName + "' failed.\n"
                        + "[" + e1.getClass().getName() + "]: " + e1.getMessage(),
                        JOptionPane.ERROR_MESSAGE);
            } finally {
                myJGnucashEditor.setCursor(Cursor.getDefaultCursor());
            }
        } catch (Exception e1) {
            LOGGER.error("Could not activate requested import-plugin '" + pluginName + "'.", e1);
            JOptionPane.showMessageDialog(myJGnucashEditor, "Error",
                    "Could not activate requested import-plugin '" + pluginName + "'.\n"
                    + "[" + e1.getClass().getName() + "]: " + e1.getMessage(),
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
