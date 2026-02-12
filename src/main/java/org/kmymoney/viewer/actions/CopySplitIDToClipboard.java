package org.kmymoney.viewer.actions;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Action;

import org.kmymoney.api.read.KMyMoneyTransactionSplit;

/*
 * Action to open an account in a new tab.
 */
public class CopySplitIDToClipboard implements TransactionSplitAction
{

    // ---------------------------------------------------------------

    // The account we open
    private KMyMoneyTransactionSplit mySplit;

    // ----------------------------

    private final Map<String, Object> myAddedTags = new HashMap<String, Object>();

    private final PropertyChangeSupport myPropertyChangeSupport = new PropertyChangeSupport(this);

    // ---------------------------------------------------------------

    public CopySplitIDToClipboard() {
        this.putValue(Action.NAME, Messages_CopySplitIDToClipboard.getString("CopySplitIDToClipboard.1"));
        this.putValue(Action.LONG_DESCRIPTION, Messages_CopySplitIDToClipboard.getString("CopySplitIDToClipboard.2"));
        this.putValue(Action.SHORT_DESCRIPTION, Messages_CopySplitIDToClipboard.getString("CopySplitIDToClipboard.3"));
    }

    public CopySplitIDToClipboard(final KMyMoneyTransactionSplit splt) {
        this();
        setSplit(splt);
    }

    // ---------------------------------------------------------------

    protected KMyMoneyTransactionSplit getSplit() {
        return mySplit;
    }

    @Override
    public void setSplit(final KMyMoneyTransactionSplit aSplit) {
        mySplit = aSplit;
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
        return getSplit() != null;
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
    	StringSelection strSel = new StringSelection(mySplit.getQualifID().toString());
    	Toolkit.getDefaultToolkit().getSystemClipboard().setContents(strSel, null);
    }

}
