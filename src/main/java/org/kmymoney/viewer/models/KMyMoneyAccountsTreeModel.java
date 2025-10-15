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

/*
 * A TreeModel representing the accounts in a KMyMoney-File.
 */
public class KMyMoneyAccountsTreeModel implements TreeModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(KMyMoneyAccountsTreeModel.class);

    public KMyMoneyAccountsTreeModel(final KMyMoneyFileImpl file) {
        super();
        setFile(file);
    }

    // The tree-root
    private KMyMoneyAccountTreeRootEntry rootEntry;

    public static class KMyMoneyAccountTreeRootEntry extends KMyMoneyAccountTreeEntry {

        // where we get our data from.
        private final KMyMoneyFileImpl file;

        public KMyMoneyAccountTreeRootEntry(final KMyMoneyFileImpl aFile) {
            super(getRootAccount(aFile));
            file = aFile;
        }

        private static KMyMoneyAccount getRootAccount(final KMyMoneyFileImpl aFile) {
            if (aFile == null) {
                throw new IllegalArgumentException("argument <aFile> is null");
            }
            
            KMyMoneyAccount root = aFile.getRootAccount();
            if (root == null) {
                throw new IllegalArgumentException("root-account is null");
            }
            
            return root;

        }

        public KMyMoneyFile getFile() {
            return file;
        }

        @Override
        public String toString() {
            return "";
        }

        @Override
        public List<? extends KMyMoneyAccount> getChildAccounts() {
        	ArrayList result = new ArrayList<KMyMoneyAccount>();
        	result.add( file.getRootAccount() );
            return result;
        }
    }

    public static class KMyMoneyAccountTreeEntry {

        // The account we represent.
        private final KMyMoneyAccount myAccount;

        public KMyMoneyAccountTreeEntry(final KMyMoneyAccount anAccount) {
            super();
            
            if (anAccount == null) {
                throw new IllegalArgumentException("argument <anAccount> is null");
            }
            
            myAccount = anAccount;
        }

        public KMyMoneyAccount getAccount() {
            return myAccount;
        }

        @Override
        public String toString() {
            String hidden = getAccount().getUserDefinedAttribute("hidden");
            if (hidden != null && hidden.equalsIgnoreCase("true")) {
                return "[hidden]" + getAccount().getName();
            }
            return getAccount().getName();
        }

        // The tree-nodes below us
        private volatile List<KMyMoneyAccountTreeEntry> childTreeNodes = null;

        public List<KMyMoneyAccountTreeEntry> getChildTreeNodes() {
            if ( childTreeNodes == null ) {
                Collection<? extends KMyMoneyAccount> c = getChildAccounts();
                childTreeNodes = new ArrayList<KMyMoneyAccountTreeEntry>(c.size());
                for ( KMyMoneyAccount kmymoneyAccount : c ) {
                    KMyMoneyAccount subaccount = kmymoneyAccount;
                    childTreeNodes.add(new KMyMoneyAccountTreeEntry(subaccount));
                }
            }

            return childTreeNodes;
        }

        public Collection<? extends KMyMoneyAccount> getChildAccounts() {
            return myAccount.getChildren();
        }
    }

    public Object getRoot() {
        return rootEntry;
    }

    public int getChildCount(final Object parent) {
    	return ((KMyMoneyAccountTreeEntry) parent).getChildTreeNodes().size();
    }

    public boolean isLeaf(final Object node) {
        return getChildCount(node) == 0;
    }

    private final Set<TreeModelListener> listeners = new HashSet<TreeModelListener>();

    public void addTreeModelListener(final TreeModelListener l) {
        listeners.add(l);

    }

    public void removeTreeModelListener(final TreeModelListener l) {
        listeners.remove(l);
    }

    public Object getChild(final Object parent, final int index) {
        return ((KMyMoneyAccountTreeEntry) parent).getChildTreeNodes().get(index);
    }

    public int getIndexOfChild(final Object parent, final Object child) {
        return ((KMyMoneyAccountTreeEntry) parent).getChildTreeNodes().indexOf(child);
    }

    public void valueForPathChanged(final TreePath path, final Object newValue) {
        // TODO unsupported
    }

    public KMyMoneyFile getFile() {
        return rootEntry.getFile();
    }

    public void setFile(final KMyMoneyFileImpl file) {
        if (file == null) {
            throw new IllegalArgumentException("argument <file> is null");
        }
        
        rootEntry = new KMyMoneyAccountTreeRootEntry(file);

        fireTreeStructureChanged(getPathToRoot());
    }

    protected TreePath getPathToRoot() {
    	return new TreePath(getRoot());
    }

    protected void fireTreeStructureChanged(final TreePath path) {
		TreeModelEvent evt = new TreeModelEvent( this, path );

		for ( TreeModelListener listener : listeners ) {
			listener.treeStructureChanged( evt );
		}
    }

}
