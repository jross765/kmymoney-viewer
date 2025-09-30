package org.kmymoney.viewer.models;



import javax.swing.table.TableModel;

import org.kmymoney.api.read.KMyMoneyTransactionSplit;


/**
 * TableModels implementing this interface contain a list of transactions.
 * They may be all transactions of an account, a search-result or sth. similar.
 */
public interface KMyMoneyTransactionsSplitsTableModel extends TableModel {

    /**
     * Get the number of transactons.
     * @return an integer >=0
     */
    int getRowCount();

    /**
     * Get the TransactionsSplit at the given index.
     * Throws an exception if the index is invalid.
     * @param rowIndex the split to get
     * @return the split
     */
    KMyMoneyTransactionSplit getTransactionSplit(final int rowIndex);


}
