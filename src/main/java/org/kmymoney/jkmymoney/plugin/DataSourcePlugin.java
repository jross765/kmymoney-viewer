package org.kmymoney.jkmymoney.plugin;

import java.io.IOException;

import org.kmymoney.api.write.KMyMoneyWritableFile;

/**
 * This is a plugin-interface that plugins that want to plug
 * into the extension-point "Importer" of the
 * "org.gnucash.jgnucash.editor.main"- plugin.<br/>
 * Extension - point declaration:<br/>
 * <pre>
    <extension-point id="DataSource"><!-- displayed in file-menu as "load <xyz>..." -->
        <parameter-def id="class" /> <!-- class must implement org.gnucash.jgnucash.plugin.DataSourcePlugin -->
        <parameter-def id="name" />
        <parameter-def id="description" multiplicity="none-or-one" />
        <parameter-def id="icon" multiplicity="none-or-one" />
        <parameter-def id="supportsWriting"/> <!-- "true" or "false" : Supports a menu-handler "write file" that writes to where it was loaded from. -->
        <parameter-def id="supportsWritingTo"/> <!-- "true" or "false" : Supports a menu-handler "write file to...". -->
    </extension-point>
  </pre>
 */
public interface DataSourcePlugin {

    /**
     * Runt the actual import.
     * @return the loaded file or null
     * @throws IOException on IO-issues
     */
    KMyMoneyWritableFile loadFile() throws IOException;

    /**
     * Write to where this file was loaded from.
     * @param file the file to write
     * @throws IOException on IO-issues
     */
    void write(KMyMoneyWritableFile file) throws IOException;

    /**
     * Let the user choose a location to write to.
     * @param file the file to write
     * @throws IOException on IO-issues
     */
    void writeTo(KMyMoneyWritableFile file) throws IOException;
}
