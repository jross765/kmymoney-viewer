package org.kmymoney.viewer.actions;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

import org.kmymoney.api.read.KMyMoneyAccount;
import org.kmymoney.api.read.KMyMoneyTransaction;
import org.kmymoney.api.read.KMyMoneyTransactionSplit;
import org.kmymoney.viewer.panels.TransactionsPanel;

/*
 * Action to open an account in a new tab.
 */
public class OpenAccountInNewWindow implements AccountAction,
        									   TransactionSplitAction
{

    // ---------------------------------------------------------------

    // The account we open.
    private KMyMoneyAccount myAccount;

    // Optional: the transaction to highlight
    private KMyMoneyTransaction myTransaction;
    
    // Optional: the transaction split to highlight
    private KMyMoneyTransactionSplit myTransactionSplit;
    
    private boolean selectSplit = false;
    
    // ----------------------------

    private final Map<String, Object> myAddedTags = new HashMap<String, Object>();

    private final PropertyChangeSupport myPropertyChangeSupport = new PropertyChangeSupport(this);
    
    // ---------------------------------------------------------------

    public OpenAccountInNewWindow() {
        this.putValue(Action.NAME, Messages_OpenAccountInNewWindow.getString("OpenAccountInNewWindow.1"));
        this.putValue(Action.LONG_DESCRIPTION, Messages_OpenAccountInNewWindow.getString("OpenAccountInNewWindow.2"));
        this.putValue(Action.SHORT_DESCRIPTION, Messages_OpenAccountInNewWindow.getString("OpenAccountInNewWindow.3"));
    }

    public OpenAccountInNewWindow(final KMyMoneyAccount anAccount) {
        this();
        setAccount(anAccount);
    }

    public OpenAccountInNewWindow(final KMyMoneyTransactionSplit aSplit, final boolean selectSplit) {
        this();
        setSplit(aSplit);
        this.selectSplit = selectSplit;
    }

    // ---------------------------------------------------------------

    @Override
    public void setAccount(final KMyMoneyAccount anAccount) {
        this.myAccount = anAccount;
        this.myTransaction = null;
        this.myTransactionSplit = null;
    }

    @Override
    public void setSplit(final KMyMoneyTransactionSplit aSplit) {
        this.myAccount = aSplit.getAccount();
        this.myTransaction = aSplit.getTransaction();
        this.myTransactionSplit = aSplit;
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
        return myAccount != null;
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
    	// ::EMPTY
    }

    @Override
    public void actionPerformed(final ActionEvent aE) {
    	TransactionsPanel newPanel = new TransactionsPanel();
        newPanel.setAccount(myAccount);
        if ( myTransactionSplit != null &&
        	 myTransaction      != null ) {
        	if ( selectSplit ) {
                newPanel.setTransactionSplit(myTransactionSplit);
        	} else {
                newPanel.setTransaction(myTransaction);
        	}
        }
        JFrame newFrame = new JFrame(myAccount.getName());
        newFrame.getContentPane().add(newPanel);
        newFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        newFrame.pack();
        newFrame.setVisible(true);
    }

}
