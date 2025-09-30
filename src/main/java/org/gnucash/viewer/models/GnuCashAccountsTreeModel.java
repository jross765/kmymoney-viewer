/**
 * GnuCashAccountsTreeModel.java
 * Created on 15.05.2005
 * (c) 2005 by "Wolschon Softwaredesign und Beratung".
 *
 *
 * -----------------------------------------------------------
 * major Changes:
 *  15.05.2005 - initial version
 * ...
 *
 */
package org.gnucash.viewer.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.gnucash.api.read.GnuCashAccount;
import org.gnucash.api.read.GnuCashFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * created: 15.05.2005 <br/>
 *
 * A TreeModel representing the accounts in a Gnucash-File.
 * @author <a href="mailto:Marcus@Wolschon.biz">Marcus Wolschon</a>
 *
 */
public class GnuCashAccountsTreeModel implements TreeModel {
    /**
     * Our logger for debug- and error-ourput.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(GnuCashAccountsTreeModel.class);

    /**
     * @param file where we get our data from
     */
    public GnuCashAccountsTreeModel(final GnuCashFile file) {
        super();
        setFile(file);
    }

    /**
     * The tree-root.
     */
    private GnuCashAccountTreeRootEntry rootEntry;

    /**
     * (c) 2009 by <a href="http://Wolschon.biz>Wolschon Softwaredesign und Beratung</a>.<br/>
     * Project: jgnucashLib-GPL<br/>
     * GnuCashAccountTreeRootEntry<br/>
     * <br/><br/>
     * <b>Helper-class representing a tree-entry.</b>
     * @author  <a href="mailto:Marcus@Wolschon.biz">fox</a>
     */
    public static class GnuCashAccountTreeRootEntry extends GnuCashAccountTreeEntry {

        /**
         * where we get our data from.
         */
        private final GnuCashFile file;

        /**
         * @param aFile where we get our data from
         */
        public GnuCashAccountTreeRootEntry(final GnuCashFile aFile) {
            super(getRootAccount(aFile));
            file = aFile;
        }
        /**
         * @param aFile where we get our data from
         * @return the root-account checked for null
         */
        private static GnuCashAccount getRootAccount(final GnuCashFile aFile) {
            if (aFile == null) {
                throw new IllegalArgumentException("null file given");
            }
            GnuCashAccount root = aFile.getRootAccount();
            if (root == null) {
                throw new IllegalArgumentException("root-account is null");
            }
            return root;

        }
        /**
         * @return where we get our data from
         */
        public GnuCashFile getFile() {
            return file;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return "";
        }

        /**
         * @return the accounts below us
         */
        @Override
        public List<? extends GnuCashAccount> getChildAccounts() {
        	ArrayList result = new ArrayList<GnuCashAccount>();
        	result.add( file.getRootAccount() );
            return result;
        }
    }

    /**
     * (c) 2009 by <a href="http://Wolschon.biz>Wolschon Softwaredesign und Beratung</a>.<br/>
     * Project: jgnucashLib-GPL<br/>
     * GnuCashAccountTreeEntry<br/>
     * <br/>
     * <b>Helper-class representing a tree-entry.</b>
     * @author  <a href="mailto:Marcus@Wolschon.biz">fox</a>
     */
    public static class GnuCashAccountTreeEntry {

        /**
         * The account we represent.
         */
        private final GnuCashAccount myAccount;

        /**
         * @param anAccount The account we represent.
         */
        public GnuCashAccountTreeEntry(final GnuCashAccount anAccount) {
            super();
            if (anAccount == null) {
                throw new IllegalArgumentException("null account given");
            }
            myAccount = anAccount;
        }
        /**
         * @return The account we represent.
         */
        public GnuCashAccount getAccount() {
            return myAccount;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            String hidden = getAccount().getUserDefinedAttribute("hidden");
            if (hidden != null && hidden.equalsIgnoreCase("true")) {
                return "[hidden]" + getAccount().getName();
            }
            return getAccount().getName();
        }

        /**
         * The tree-nodes below us.
         */
        private volatile List<GnuCashAccountTreeEntry> childTreeNodes = null;

        /**
         * {@inheritDoc}
         */
        public List<GnuCashAccountTreeEntry> getChildTreeNodes() {

            if (childTreeNodes == null) {
                Collection<? extends GnuCashAccount> c = getChildAccounts();
                childTreeNodes = new ArrayList<GnuCashAccountTreeEntry>(c.size());
                for (GnuCashAccount gnucashAccount : c) {
                    GnuCashAccount subaccount = gnucashAccount;
                    childTreeNodes.add(new GnuCashAccountTreeEntry(subaccount));
                }
            }

            return childTreeNodes;
        }

        /**
         * @return See {@link GnuCashAccount#getChildren()}
         */
        public Collection<? extends GnuCashAccount> getChildAccounts() {
            return myAccount.getChildren();
        }
    }

    /**
     * {@inheritDoc}
     * @see javax.swing.tree.TreeModel#getRoot()
     */
    public Object getRoot() {
        return rootEntry;
    }

    /**
     * {@inheritDoc}
     * @see javax.swing.tree.TreeModel#getChildCount(java.lang.Object)
     */
    public int getChildCount(final Object parent) {
        return ((GnuCashAccountTreeEntry) parent).getChildTreeNodes().size();
    }

    /**
     * {@inheritDoc}
     * @see javax.swing.tree.TreeModel#isLeaf(java.lang.Object)
     */
    public boolean isLeaf(final Object node) {
        return getChildCount(node) == 0;
    }


    /**
     * Our {@link TreeModelListener}s.
     */
    private final Set<TreeModelListener> listeners = new HashSet<TreeModelListener>();

    /**
     * {@inheritDoc}
     * @see javax.swing.tree.TreeModel#addTreeModelListener(javax.swing.event.TreeModelListener)
     */
    public void addTreeModelListener(final TreeModelListener l) {
        listeners.add(l);

    }

    /**
     * {@inheritDoc}
     * @see javax.swing.tree.TreeModel#removeTreeModelListener(javax.swing.event.TreeModelListener)
     */
    public void removeTreeModelListener(final TreeModelListener l) {
        listeners.remove(l);

    }

    /**
     * {@inheritDoc}
     * @see javax.swing.tree.TreeModel#getChild(java.lang.Object, int)
     */
    public Object getChild(final Object parent, final int index) {
        return ((GnuCashAccountTreeEntry) parent).getChildTreeNodes().get(index);
    }

    /**
     * {@inheritDoc}
     * @see javax.swing.tree.TreeModel#getIndexOfChild(java.lang.Object, java.lang.Object)
     */
    public int getIndexOfChild(final Object parent, final Object child) {
        return ((GnuCashAccountTreeEntry) parent).getChildTreeNodes().indexOf(child);
    }

    /**
     * {@inheritDoc}
     * @see javax.swing.tree.TreeModel#valueForPathChanged(javax.swing.tree.TreePath, java.lang.Object)
     */
    public void valueForPathChanged(final TreePath path, final Object newValue) {
        // TODO unsupported

    }

    /**
     * @return The gnucash-file we work on.
     */
    public GnuCashFile getFile() {
        return rootEntry.getFile();
    }
    /**
     * @param file The gnucash-file we work on.
     */
    public void setFile(final GnuCashFile file) {
        if (file == null) {
            throw new IllegalArgumentException(
                    "null not allowed for field this.file");
        }
       rootEntry = new GnuCashAccountTreeRootEntry(file);

        fireTreeStructureChanged(getPathToRoot());
    }

    /**
     * {@inheritDoc}
     */
    protected TreePath getPathToRoot() {
     return new TreePath(getRoot());
    }

    /**
     * @param path the path to inform our {@link TreeModelListener}s about.
     */
    protected void fireTreeStructureChanged(final TreePath path) {
     TreeModelEvent evt = new TreeModelEvent(this, path);

     for (TreeModelListener listener : listeners) {
        listener.treeStructureChanged(evt);

    }
    }

}
