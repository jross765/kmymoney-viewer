/**
 * KMyMoneyTransactionsSplitsTableModel.java
 * created: 21.10.2006 17:38:52
 */
package org.kmymoney.viewer.models;



import javax.swing.table.TableModel;

import org.kmymoney.api.read.KMyMoneyTransactionSplit;


/**
 * (c) 2006 by Wolschon Softwaredesign und Beratung.<br/>
 * Project: kmymoneyReader<br/>
 * KMyMoneyTransactionsSplitsTableModel.java<br/>
 * created: 21.10.2006 17:38:52 <br/>
 *<br/><br/>
 * <b>TableModels implementing this interface contain a list of transactions.
 * They may be all transactions of an account, a search-result or sth. similar.</b>
 * @author <a href="Marcus@Wolschon.biz">Marcus Wolschon</a>
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
