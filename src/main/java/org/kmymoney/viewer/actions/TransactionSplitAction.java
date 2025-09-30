package org.kmymoney.viewer.actions;

import javax.swing.Action;

import org.kmymoney.api.read.KMyMoneyTransactionSplit;

/**
 * Action that can be executed on a {@link KMyMoneyTransactionSplit}
 */
public interface TransactionSplitAction extends Action {

    /**
     * Set the split this action works on.
     * @param aSplit the split to work.
     */
    void setSplit(final KMyMoneyTransactionSplit aSplit);
}
