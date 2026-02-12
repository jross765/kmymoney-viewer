package org.kmymoney.viewer.actions;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Action;

import org.kmymoney.api.read.KMyMoneyAccount;
import org.kmymoney.api.read.KMyMoneyTransactionSplit;

/*
 * Action to open an account in a new tab.
 */
public class CopyAccountIDToClipboard implements AccountAction,
											     TransactionSplitAction
{

    // ---------------------------------------------------------------

    // The account we open
    private KMyMoneyAccount myAccount;

    // ----------------------------

    private final Map<String, Object> myAddedTags = new HashMap<String, Object>();

    private final PropertyChangeSupport myPropertyChangeSupport = new PropertyChangeSupport(this);

    // ---------------------------------------------------------------

    public CopyAccountIDToClipboard() {
        this.putValue(Action.NAME, Messages_CopyAccountIDToClipboard.getString("CopyAccountIDToClipboard.1"));
        this.putValue(Action.LONG_DESCRIPTION, Messages_CopyAccountIDToClipboard.getString("CopyAccountIDToClipboard.2"));
        this.putValue(Action.SHORT_DESCRIPTION, Messages_CopyAccountIDToClipboard.getString("CopyAccountIDToClipboard.3"));
    }

    public CopyAccountIDToClipboard(final KMyMoneyAccount acct) {
        this();
        setAccount(acct);
    }

    public CopyAccountIDToClipboard(final KMyMoneyTransactionSplit splt) {
        this();
        setSplit(splt);
    }

    // ---------------------------------------------------------------

    protected KMyMoneyAccount getAccount() {
        return myAccount;
    }

    @Override
    public void setAccount(final KMyMoneyAccount anAccount) {
        myAccount = anAccount;
    }

    @Override
    public void setSplit(final KMyMoneyTransactionSplit aSplit) {
        myAccount = aSplit.getAccount();
    }

    // ---------------------------------------------------------------

    @Override
    public void addPropertyChangeListener(final PropertyChangeListener aListener) {
        myPropertyChangeSupport.addPropertyChangeListener(aListener);
    }

    @Override
    public Object getValue(final String aKey) {
        return myAddedTags.get(aKey);
    }

    @Override
    public boolean isEnabled() {
        return getAccount() != null;
    }

    @Override
    public void putValue(final String aKey, final Object aValue) {
        myAddedTags.put(aKey, aValue);
    }

    @Override
    public void removePropertyChangeListener(final PropertyChangeListener aListener) {
        myPropertyChangeSupport.removePropertyChangeListener(aListener);
    }

    @Override
    public void setEnabled(final boolean aB) {
    }

    @Override
    public void actionPerformed(final ActionEvent aE) {
    	StringSelection strSel = new StringSelection(myAccount.getID().toString());
    	Toolkit.getDefaultToolkit().getSystemClipboard().setContents(strSel, null);
    }

}
