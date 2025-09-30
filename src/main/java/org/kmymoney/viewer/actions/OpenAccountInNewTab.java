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

/**
 * Action to open an account in a new tab.
 */
public class OpenAccountInNewTab implements AccountAction,
		org.kmymoney.viewer.actions.TransactionSplitAction {

    /**
     * The account we open.
     */
    private KMyMoneyAccount myAccount;

    /**
     * @see #getValue(String)
     */
    private final Map<String, Object> myAddedTags = new HashMap<String, Object>();

    /**
     * @see #addPropertyChangeListener(PropertyChangeListener)
     */
    private final PropertyChangeSupport myPropertyChangeSupport = new PropertyChangeSupport(this);

    /**
     * Optional the transaction to highlight.
     */
    private KMyMoneyTransaction myTransaction;

    /**
     * The TabbedPane to open in.
     */
    private final JTabbedPane myTabbedPane;

    /**
     * Initialize.
     * @param aTabbedPane The TabbedPane to open in.
     */
    public OpenAccountInNewTab(final JTabbedPane aTabbedPane) {
        this.putValue(Action.NAME, "Open Account in new Tab");
        this.putValue(Action.LONG_DESCRIPTION, "Open the given Account in new Tab.");
        this.putValue(Action.SHORT_DESCRIPTION, "Open Account in new Tab.");
        myTabbedPane = aTabbedPane;
    }
    /**
     * @param aSplit the split to show the account of.
     * @param aTabbedPane The TabbedPane to open in.
     */
    public OpenAccountInNewTab(final JTabbedPane aTabbedPane, final KMyMoneyTransactionSplit aSplit) {
        this(aTabbedPane);
        setSplit(aSplit);
    }
    /**
     * @param anAccount the account to show.
     * @param aTabbedPane The TabbedPane to open in.
     */
    public OpenAccountInNewTab(final JTabbedPane aTabbedPane, final KMyMoneyAccount anAccount) {
        this(aTabbedPane);
        setAccount(anAccount);
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public void setAccount(final KMyMoneyAccount anAccount) {
        myAccount = anAccount;
        myTransaction = null;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public void setSplit(final KMyMoneyTransactionSplit aSplit) {
        myAccount = aSplit.getAccount();
        myTransaction = aSplit.getTransaction();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addPropertyChangeListener(final PropertyChangeListener aListener) {
        myPropertyChangeSupport.addPropertyChangeListener(aListener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getValue(final String aKey) {
        return myAddedTags.get(aKey);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEnabled() {
        return getAccount() != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void putValue(final String aKey, final Object aValue) {
        myAddedTags.put(aKey, aValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removePropertyChangeListener(final PropertyChangeListener aListener) {
        myPropertyChangeSupport.removePropertyChangeListener(aListener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setEnabled(final boolean aB) {
    }

    /**
     * {@inheritDoc}
     */
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
    /**
     * @param tabName the label of the tab
     * @param tabContent the content
     */
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
    /**
     * @return the account
     */
    protected KMyMoneyAccount getAccount() {
        return myAccount;
    }


}
