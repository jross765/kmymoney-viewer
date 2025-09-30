package org.kmymoney.jkmymoney.actions;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;

import org.java.plugin.registry.Extension;
import org.kmymoney.jkmymoney.JKMyMoney;
import org.kmymoney.jkmymoney.plugin.DataSourcePlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The action-listeners we use for the File->Save as -Menu.
 */
public final class SaveAsFilePluginMenuAction implements ActionListener {
    /**
     * Our logger for debug- and error-output.
     */
    static final Logger LOGGER = LoggerFactory.getLogger(SaveAsFilePluginMenuAction.class);

    /**
     * Our JKMyMoney.java.
     * @see OpenFilePluginMenuAction
     */
    private final JKMyMoney myJKMyMoneyEditor;
    /**
     * @param aPlugin The import-plugin.
     * @param aPluginName The name of the plugin
     * @param aKMyMoney TODO
     */
    public SaveAsFilePluginMenuAction(final JKMyMoney aKMyMoney, final Extension aPlugin, final String aPluginName) {
        super();
        myJKMyMoneyEditor = aKMyMoney;
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

            // Activate plug-in that declares extension.
            myJKMyMoneyEditor.getPluginManager().activatePlugin(ext.getDeclaringPluginDescriptor().getId());
            // Get plug-in class loader.
            ClassLoader classLoader = myJKMyMoneyEditor.getPluginManager().getPluginClassLoader(
                                      ext.getDeclaringPluginDescriptor());
            // Load Tool class.
            Class toolCls = classLoader.loadClass(
                    ext.getParameter("class").valueAsString());
            // Create Tool instance.
            Object o = toolCls.newInstance();
            if (!(o instanceof DataSourcePlugin)) {
                LOGGER.error("Plugin '" + pluginName + "' does not implement DataSourcePlugin-interface.");
                JOptionPane.showMessageDialog(myJKMyMoneyEditor, "Error",
                        "Plugin '" + pluginName + "' does not implement DataSourcePlugin-interface.",
                        JOptionPane.ERROR_MESSAGE);
                return;

            }
            DataSourcePlugin importer = (DataSourcePlugin) o;
            try {
                myJKMyMoneyEditor.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                importer.writeTo(myJKMyMoneyEditor.getWritableModel());
            } catch (Exception e1) {
                LOGGER.error("Write via Plugin '" + pluginName + "' failed.", e1);
                JOptionPane.showMessageDialog(myJKMyMoneyEditor, "Error",
                        "Write via Plugin '" + pluginName + "' failed.\n"
                        + "[" + e1.getClass().getName() + "]: " + e1.getMessage(),
                        JOptionPane.ERROR_MESSAGE);
            } finally {
                myJKMyMoneyEditor.setCursor(Cursor.getDefaultCursor());
            }
        } catch (Exception e1) {
            LOGGER.error("Could not activate requested Writer-plugin '" + pluginName + "'.", e1);
            JOptionPane.showMessageDialog(myJKMyMoneyEditor, "Error",
                    "Could not activate requested Writer-plugin '" + pluginName + "'.\n"
                    + "[" + e1.getClass().getName() + "]: " + e1.getMessage(),
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
