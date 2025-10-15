package org.kmymoney.viewer.actions;

import javax.swing.Action;

import org.kmymoney.api.read.KMyMoneyTransactionSplit;

/*
 * Action that can be executed on a KMyMoneyTransactionSplit
 */
public interface TransactionSplitAction extends Action {

    // Set the split this action works on.
    void setSplit(final KMyMoneyTransactionSplit aSplit);
}
