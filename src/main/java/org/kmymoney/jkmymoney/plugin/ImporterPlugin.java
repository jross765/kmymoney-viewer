package org.kmymoney.jkmymoney.plugin;

import org.kmymoney.api.write.KMyMoneyWritableAccount;
import org.kmymoney.api.write.KMyMoneyWritableFile;

/**
 * This is a plugin-interface that plugins that want to plug
 * into the extension-point "Importer" of the
 * "org.gnucash.jgnucash.editor.main"- plugin.<br/>
 * Extension - point declaration:<br/>
 * <pre>
 * <extension-point id="Importer">
        <parameter-def id="class" /> (classname of class implementing this interface)
        <parameter-def id="name" /> (name to be displayed in menus,...)
        <parameter-def id="hasFixedAccount" /> <!-- false=use currently selected account -->
        <parameter-def id="description" multiplicity="none-or-one" /> (optionsl, very short description)
        <parameter-def id="icon" multiplicity="none-or-one" /> (optional path to icon-ressource)
    </extension-point>
    </pre>
 */
public interface ImporterPlugin {

    /**
     * Runt the actual import.
     * @param writableModel the gnucash-file to import to.
     * @param currentAccount the currently selected account (may be null)
     * @return (optional) a message to be displayed after success (or null/empty message)
     */
    String runImport(final KMyMoneyWritableFile writableModel, final KMyMoneyWritableAccount currentAccount) throws Exception;

}
