package org.kmymoney.viewer.actions;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.kmymoney.api.read.KMyMoneyAccount;
import org.kmymoney.api.read.KMyMoneyTransaction;
import org.kmymoney.api.read.KMyMoneyTransactionSplit;
import org.kmymoney.viewer.panels.TransactionsPanel;

/*
 * Action to open an account in a new tab.
 */
public class OpenAccountInNewTab implements AccountAction,
		org.kmymoney.viewer.actions.TransactionSplitAction {

    // The account we open
    private KMyMoneyAccount myAccount;

    private final Map<String, Object> myAddedTags = new HashMap<String, Object>();

    private final PropertyChangeSupport myPropertyChangeSupport = new PropertyChangeSupport(this);

    // Optional: the transaction to highlight.
    private KMyMoneyTransaction myTransaction;

    // The tabbed pane to open the account in
    private final JTabbedPane myTabbedPane;

    /*
     * Initialize.
     */
    public OpenAccountInNewTab(final JTabbedPane aTabbedPane) {
        this.putValue(Action.NAME, Messages_OpenAccountInNewTab.getString("OpenAccountInNewTab.1"));
        this.putValue(Action.LONG_DESCRIPTION, Messages_OpenAccountInNewTab.getString("OpenAccountInNewTab.2"));
        this.putValue(Action.SHORT_DESCRIPTION, Messages_OpenAccountInNewTab.getString("OpenAccountInNewTab.3"));
        myTabbedPane = aTabbedPane;
    }

    public OpenAccountInNewTab(final JTabbedPane aTabbedPane, final KMyMoneyTransactionSplit aSplit) {
        this(aTabbedPane);
        setSplit(aSplit);
    }

    public OpenAccountInNewTab(final JTabbedPane aTabbedPane, final KMyMoneyAccount anAccount) {
        this(aTabbedPane);
        setAccount(anAccount);
    }

    @Override
    public void setAccount(final KMyMoneyAccount anAccount) {
        myAccount = anAccount;
        myTransaction = null;
    }

    @Override
    public void setSplit(final KMyMoneyTransactionSplit aSplit) {
        myAccount = aSplit.getAccount();
        myTransaction = aSplit.getTransaction();
    }

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
        final TransactionsPanel newTransactionsPanel = new TransactionsPanel();
        newTransactionsPanel.setAccount(getAccount());
        if (myTransaction != null) {
            newTransactionsPanel.setTransaction(myTransaction);
        }
        String tabName = getAccount().getName();
        addTab(tabName, newTransactionsPanel);
    }

    private void addTab(final String tabName, final JComponent tabContent) {

        myTabbedPane.addTab(null, tabContent);
        JPanel tab = new JPanel(new BorderLayout(2, 0));
        tab.setOpaque(false);
        tab.add(new JLabel(tabName), BorderLayout.CENTER);
        JButton closeButton = new JButton("X");
        closeButton.setBorder(BorderFactory.createEmptyBorder());
        final int size = 10;
        closeButton.setPreferredSize(new Dimension(size, size));
        closeButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent aE) {
                myTabbedPane.remove(tabContent);
            }

        });
        tab.add(closeButton, BorderLayout.EAST);
        myTabbedPane.setTabComponentAt(myTabbedPane.getTabCount() - 1, tab);
    }

    protected KMyMoneyAccount getAccount() {
        return myAccount;
    }

}
