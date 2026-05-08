package org.kmymoney.viewer.models;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.event.TableModelListener;

import org.apache.commons.numbers.fraction.BigFraction;
import org.kmymoney.api.read.KMyMoneyAccount;
import org.kmymoney.api.read.KMyMoneyTransactionSplit;
import org.kmymoney.api.read.impl.KMyMoneyAccountImpl;
import org.kmymoney.viewer.GUIServices;

/**
 * A Table model that shows the transaction and balance of an Account.
 */
public class KMyMoneySimpleAccountTransactionsTableModel implements KMyMoneyTransactionSplitsTableModel {

	enum TableCols {
		DATE,
		TRANSACTION,
		DESCRIPTION,
		PLUS,
		MINUS,
		BALANCE
	}

	// ---------------------------------------------------------------

	// How to format dates
	public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_DATE;
	
	// *Not* for formatting currencies, but for computing min/max *string length* of
	// formatted currencies:
	public static final NumberFormat DEFAULT_CURRENCY_FORMAT = NumberFormat.getCurrencyInstance();

	// ---------------------------------------------------------------

	// The account the transactions of which we are showing.
	private final KMyMoneyAccount acct;

	// The columns we display.
	private final String[] defaultColumnNames = new String[] {
			Messages_KMyMoneySimpleAccountTransactionsTableModel.getString("KMyMoneySimpleAccountTransactionsTableModel.1"), 
			Messages_KMyMoneySimpleAccountTransactionsTableModel.getString("KMyMoneySimpleAccountTransactionsTableModel.2"), 
			Messages_KMyMoneySimpleAccountTransactionsTableModel.getString("KMyMoneySimpleAccountTransactionsTableModel.3"), 
			Messages_KMyMoneySimpleAccountTransactionsTableModel.getString("KMyMoneySimpleAccountTransactionsTableModel.4"), 
			Messages_KMyMoneySimpleAccountTransactionsTableModel.getString("KMyMoneySimpleAccountTransactionsTableModel.5"), 
			Messages_KMyMoneySimpleAccountTransactionsTableModel.getString("KMyMoneySimpleAccountTransactionsTableModel.6")
		};

	// ---------------------------------------------------------------

	/**
	 * the Table will be empty.
	 *
	 */
	public KMyMoneySimpleAccountTransactionsTableModel() {
		super();
		acct = null;
	}

	/**
	 * @param anAccount the account the splits of which to display.
	 */
	public KMyMoneySimpleAccountTransactionsTableModel(final KMyMoneyAccount anAccount) {
		super();
		acct = anAccount;
	}

	// ---------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	public int getColumnCount() {
		return defaultColumnNames.length;
	}

	/**
	 * {@inheritDoc}
	 */
	public int getRowCount() {
		List<? extends KMyMoneyTransactionSplit> transactionSplits = getTransactionSplits();
		if ( transactionSplits == null ) {
			return 0;
		}
		return transactionSplits.size();
	}

	/**
	 * @return the splits that affect this account.
	 */
	public List<? extends KMyMoneyTransactionSplit> getTransactionSplits() {
		if ( acct == null ) {
			return new LinkedList<KMyMoneyTransactionSplit>();
		}
		
		return acct.getTransactionSplits();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isCellEditable(final int rowIndex, final int columnIndex) {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public Class getColumnClass(final int columnIndex) {
		return String.class;
	}

	/**
	 * Get the TransactionsSplit at the given index.
	 * Throws an exception if the index is invalid.
	 * @param rowIndex the split to get
	 * @return the split
	 */
	public KMyMoneyTransactionSplit getTransactionSplit(final int rowIndex) {
		KMyMoneyTransactionSplit split = getTransactionSplits().get(rowIndex);
		return split;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getValueAt(final int rowIndex, final int columnIndex) {
		try {
			KMyMoneyTransactionSplit split = getTransactionSplit(rowIndex);

			if ( columnIndex == TableCols.DATE.ordinal() ) {
				return split.getTransaction().getDatePostedFormatted();
			} else if ( columnIndex == TableCols.TRANSACTION.ordinal() ) {
				String desc = split.getTransaction().getMemo();
				if (desc == null || desc.trim().length() == 0) {
					return "";
				}
				return desc;
			} else if ( columnIndex == TableCols.DESCRIPTION.ordinal() ) {
				String desc = split.getMemo();
				if (desc == null || desc.trim().length() == 0) {
					return "";
				}
				return desc;
			} else if ( columnIndex == TableCols.PLUS.ordinal() ) {
				if ( split.getSharesRat().compareTo(BigFraction.ZERO) >= 0 ) {
					return split.getSharesFormatted();
				} else {
					return "";
				}
			} else if ( columnIndex == TableCols.MINUS.ordinal() ) {
				if ( split.getSharesRat().compareTo(BigFraction.ZERO) < 0 ) {
					return split.getSharesFormatted();
				} else {
					return "";
				}
			} else if ( columnIndex == TableCols.BALANCE.ordinal() ) {
				if ( acct != null ) {
					return GUIServices.formatBalance((KMyMoneyAccountImpl) acct, acct.getBalanceRat(split));
				} else {
					return GUIServices.formatBalance((KMyMoneyAccountImpl) acct, split.getAccount().getBalanceRat(split));
				}
			} else {
				throw new IllegalArgumentException("illegal column index " + columnIndex);
			}
		} catch (Exception x) {
			String message = "Internal Error in "
					+ getClass().getName() + ":getValueAt(int rowIndex="
					+ rowIndex
					+ ", int columnIndex="
					+ columnIndex
					+ ")!\n"
					+ "Exception of Type [" + x.getClass().getName() + "]\n"
					+ "\"" + x.getMessage() + "\"";
			StringWriter trace = new StringWriter();
			PrintWriter pw = new PrintWriter(trace);
			x.printStackTrace(pw);
			pw.close();
			message += trace.getBuffer();

			final String message2 = message;
			System.err.println(message);
			Runnable runnable = new Runnable() {
				public void run() {
					JOptionPane.showMessageDialog(null, message2);
				}
			};
			new Thread(runnable).start();
			return "ERROR";
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex) {
		// ::EMPTY
	}

	/**
	 * {@inheritDoc}
	 */
	public String getColumnName(final int columnIndex) {
		return defaultColumnNames[columnIndex];
	}

	/**
	 * @see #addTableModelListener(TableModelListener)
	 */
	private final Set<TableModelListener> myTableModelListeners = new HashSet<TableModelListener>();

	/**
	 * @see #addTableModelListener(TableModelListener)
	 * @return the tableModelListeners
	 */
	protected Set<TableModelListener> getTableModelListeners() {
		return myTableModelListeners;
	}

	/**
	 *
	 * @see javax.swing.table.TableModel#addTableModelListener(javax.swing.event.TableModelListener)
	 */
	public void addTableModelListener(final TableModelListener l) {
		myTableModelListeners.add(l);
	}

	/**
	 *
	 * @see javax.swing.table.TableModel#removeTableModelListener(javax.swing.event.TableModelListener)
	 */
	public void removeTableModelListener(final TableModelListener l) {
		myTableModelListeners.remove(l);
	}

}
