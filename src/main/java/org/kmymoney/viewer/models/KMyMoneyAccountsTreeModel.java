package org.kmymoney.viewer.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.kmymoney.api.read.KMyMoneyAccount;
import org.kmymoney.api.read.KMyMoneyFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A TreeModel representing the accounts in a KMyMoney-File.
 */
public class KMyMoneyAccountsTreeModel implements TreeModel {
    /**
     * Our logger for debug- and error-ourput.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(KMyMoneyAccountsTreeModel.class);

    /**
     * @param file where we get our data from
     */
    public KMyMoneyAccountsTreeModel(final KMyMoneyFile file) {
        super();
        setFile(file);
    }

    /**
     * The tree-root.
     */
    private KMyMoneyAccountTreeRootEntry rootEntry;

    /**
     * (c) 2009 by <a href="http://Wolschon.biz>Wolschon Softwaredesign und Beratung</a>.<br/>
     * Project: jkmymoneyLib-GPL<br/>
     * KMyMoneyAccountTreeRootEntry<br/>
     * <br/><br/>
     * <b>Helper-class representing a tree-entry.</b>
     * @author  <a href="mailto:Marcus@Wolschon.biz">fox</a>
     */
    public static class KMyMoneyAccountTreeRootEntry extends KMyMoneyAccountTreeEntry {

        /**
         * where we get our data from.
         */
        private final KMyMoneyFile file;

        /**
         * @param aFile where we get our data from
         */
        public KMyMoneyAccountTreeRootEntry(final KMyMoneyFile aFile) {
            super(getRootAccount(aFile));
            file = aFile;
        }
        /**
         * @param aFile where we get our data from
         * @return the root-account checked for null
         */
        private static KMyMoneyAccount getRootAccount(final KMyMoneyFile aFile) {
            if (aFile == null) {
                throw new IllegalArgumentException("null file given");
            }
            KMyMoneyAccount root = aFile.getRootAccount();
            if (root == null) {
                throw new IllegalArgumentException("root-account is null");
            }
            return root;

        }
        /**
         * @return where we get our data from
         */
        public KMyMoneyFile getFile() {
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
        public List<? extends KMyMoneyAccount> getChildAccounts() {
        	ArrayList result = new ArrayList<KMyMoneyAccount>();
        	result.add( file.getRootAccount() );
            return result;
        }
    }

    /**
     * (c) 2009 by <a href="http://Wolschon.biz>Wolschon Softwaredesign und Beratung</a>.<br/>
     * Project: jkmymoneyLib-GPL<br/>
     * KMyMoneyAccountTreeEntry<br/>
     * <br/>
     * <b>Helper-class representing a tree-entry.</b>
     * @author  <a href="mailto:Marcus@Wolschon.biz">fox</a>
     */
    public static class KMyMoneyAccountTreeEntry {

        /**
         * The account we represent.
         */
        private final KMyMoneyAccount myAccount;

        /**
         * @param anAccount The account we represent.
         */
        public KMyMoneyAccountTreeEntry(final KMyMoneyAccount anAccount) {
            super();
            if (anAccount == null) {
                throw new IllegalArgumentException("null account given");
            }
            myAccount = anAccount;
        }
        /**
         * @return The account we represent.
         */
        public KMyMoneyAccount getAccount() {
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
        private volatile List<KMyMoneyAccountTreeEntry> childTreeNodes = null;

        /**
         * {@inheritDoc}
         */
        public List<KMyMoneyAccountTreeEntry> getChildTreeNodes() {

            if (childTreeNodes == null) {
                Collection<? extends KMyMoneyAccount> c = getChildAccounts();
                childTreeNodes = new ArrayList<KMyMoneyAccountTreeEntry>(c.size());
                for (KMyMoneyAccount kmymoneyAccount : c) {
                    KMyMoneyAccount subaccount = kmymoneyAccount;
                    childTreeNodes.add(new KMyMoneyAccountTreeEntry(subaccount));
                }
            }

            return childTreeNodes;
        }

        /**
         * @return See {@link KMyMoneyAccount#getChildren()}
         */
        public Collection<? extends KMyMoneyAccount> getChildAccounts() {
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
        return ((KMyMoneyAccountTreeEntry) parent).getChildTreeNodes().size();
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
        return ((KMyMoneyAccountTreeEntry) parent).getChildTreeNodes().get(index);
    }

    /**
     * {@inheritDoc}
     * @see javax.swing.tree.TreeModel#getIndexOfChild(java.lang.Object, java.lang.Object)
     */
    public int getIndexOfChild(final Object parent, final Object child) {
        return ((KMyMoneyAccountTreeEntry) parent).getChildTreeNodes().indexOf(child);
    }

    /**
     * {@inheritDoc}
     * @see javax.swing.tree.TreeModel#valueForPathChanged(javax.swing.tree.TreePath, java.lang.Object)
     */
    public void valueForPathChanged(final TreePath path, final Object newValue) {
        // TODO unsupported

    }

    /**
     * @return The kmymoney-file we work on.
     */
    public KMyMoneyFile getFile() {
        return rootEntry.getFile();
    }
    /**
     * @param file The kmymoney-file we work on.
     */
    public void setFile(final KMyMoneyFile file) {
        if (file == null) {
            throw new IllegalArgumentException(
                    "null not allowed for field this.file");
        }
       rootEntry = new KMyMoneyAccountTreeRootEntry(file);

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
