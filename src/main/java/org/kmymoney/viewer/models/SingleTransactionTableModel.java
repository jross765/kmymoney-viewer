package org.kmymoney.viewer.models;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.event.TableModelListener;

import org.kmymoney.api.Const;
import org.kmymoney.api.read.KMyMoneyTransaction;
import org.kmymoney.api.read.KMyMoneyTransactionSplit;
import org.kmymoney.api.read.KMyMoneyTransactionSplit.Action;

/**
 * TableModel to show and edit the splits and details of a single transaction.
 */
public class SingleTransactionTableModel implements KMyMoneyTransactionSplitsTableModel {

	enum TableCols {
		DATE,
		ACTION,
		DESCRIPTION,
		ACCOUNT,
		PLUS,
		MINUS
	}

	// The transaction that we are showing
	private KMyMoneyTransaction myTransaction;

	// The columns we display
	private final String[] defaultColumnNames = new String[] {
			Messages_SingleTransactionTableModel.getString("SingleTransactionTableModel.1"), 
			Messages_SingleTransactionTableModel.getString("SingleTransactionTableModel.2"), 
			Messages_SingleTransactionTableModel.getString("SingleTransactionTableModel.3"), 
			Messages_SingleTransactionTableModel.getString("SingleTransactionTableModel.4"), 
			Messages_SingleTransactionTableModel.getString("SingleTransactionTableModel.5"), 
			Messages_SingleTransactionTableModel.getString("SingleTransactionTableModel.6"), 
		};

	// How to format dates
	public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern(Const.STANDARD_DATE_FORMAT);

	// How to format currencies
	public static final NumberFormat DEFAULT_CURRENCY_FORMAT = NumberFormat.getCurrencyInstance();

	public SingleTransactionTableModel(final KMyMoneyTransaction trx) {
		super();
		myTransaction = trx;
	}

	public boolean isMultiCurrency() {
		if ( getTransaction() == null ) {
			return false;
		}

		for ( KMyMoneyTransactionSplit splt : getTransaction().getSplits() ) {
			if ( splt.getAccount().getQualifSecCurrID().getType() != getTransaction().getQualifSecCurrID().getType() || 
				 ! splt.getAccount().getQualifSecCurrID().equals(getTransaction().getQualifSecCurrID()) ) {
				return true;
			}
		}

		return false;
	}

	public SingleTransactionTableModel() {
		super();
		myTransaction = null;
	}

	public KMyMoneyTransaction getTransaction() {
		return myTransaction;
	}

	public void setTransaction(final KMyMoneyTransaction trx) {
		if ( trx == null ) {
			throw new IllegalArgumentException("argument <trx> is null");
		}

		Object old = myTransaction;
		if ( old == trx ) {
			return; // nothing has changed
		}
		
		myTransaction = trx;
	}

	public KMyMoneyTransactionSplit getTransactionSplit(final int aRowIndex) {
		return getTransactionSplits().get(aRowIndex);
	}

	public List<KMyMoneyTransactionSplit> getTransactionSplits() {
		KMyMoneyTransaction transaction = getTransaction();
		if ( transaction == null ) {
			return new LinkedList<KMyMoneyTransactionSplit>();
		}
		
		return new ArrayList<KMyMoneyTransactionSplit>(transaction.getSplits());
	}

	public int getColumnCount() {
		return defaultColumnNames.length;
	}

	public int getRowCount() {
		KMyMoneyTransaction transaction = getTransaction();
		if ( transaction == null ) {
			return 0;
		}
		return 1 + getTransactionSplits().size();
	}

	@SuppressWarnings("unchecked")
	public Class getColumnClass(final int columnIndex) {
		return String.class;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	public Object getValueAt(final int rowIndex, final int columnIndex) {
		// "date", "action", "description", "account", "+", "-"
		try {
			if ( rowIndex == 0 ) {
				// show data of transaction
				if ( columnIndex == TableCols.DATE.ordinal() )
					return getTransaction().getDatePostedFormatted();
				else if ( columnIndex == TableCols.ACTION.ordinal() )
					// ::TODO
					// return getTransactionNumber();
					return "";
				else if ( columnIndex == TableCols.DESCRIPTION.ordinal() )
					return getTransactionDescription();
				else if ( columnIndex == TableCols.ACCOUNT.ordinal() )
					return "";
				else if ( columnIndex == TableCols.PLUS.ordinal() )
					return "";
				else if ( columnIndex == TableCols.MINUS.ordinal() )
					return "";
				else
					throw new IllegalArgumentException("illegal column index " + columnIndex);
			}

			KMyMoneyTransactionSplit splt = getTransactionSplit(rowIndex - 1);

			if ( columnIndex == TableCols.DATE.ordinal() ) {
				return splt.getTransaction().getDatePostedFormatted();
			} else if ( columnIndex == TableCols.ACTION.ordinal() ) {
				Action action = splt.getAction();
				if ( action == null ) {
					return "";
				}
				return action;
			} else if ( columnIndex == TableCols.DESCRIPTION.ordinal() ) {
				String desc = splt.getMemo();
				if ( desc == null || 
					 desc.trim().length() == 0 ) {
					return "";
				}
				return desc;
			} else if ( columnIndex == TableCols.ACCOUNT.ordinal() ) {
				return splt.getAccount().getQualifiedName();
			} else if ( columnIndex == TableCols.PLUS.ordinal() ) {
				if ( splt.getValue().isPositive() ) {
					if ( splt.getAccount().getQualifSecCurrID().getType() == getTransaction().getQualifSecCurrID().getType() && 
						 splt.getAccount().getQualifSecCurrID().equals(getTransaction().getQualifSecCurrID()) ) {
						return splt.getValueFormatted();
					}
					return splt.getValueFormatted() + " (" + splt.getSharesFormatted() + ")";
				} else {
					return "";
				}
			} else if ( columnIndex == TableCols.MINUS.ordinal() ) {
				if ( ! splt.getValue().isPositive() ) {
					if ( splt.getAccount().getQualifSecCurrID().getType() == getTransaction().getQualifSecCurrID().getType() && 
						 splt.getAccount().getQualifSecCurrID().equals(getTransaction().getQualifSecCurrID()) ) {
						return splt.getValueFormatted();
					}
					return splt.getValueFormatted() + " (" + splt.getSharesFormatted() + ")";
				} else {
					return "";
				}
			} else {
				throw new IllegalArgumentException("illegal columnIndex " + columnIndex);
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

			System.err.println(message);
			JOptionPane.showMessageDialog(null, message);
			return "ERROR";
		}
	}

	/**
	 * @return the description of the transaction as we display it. Never null.
	 */
	private Object getTransactionDescription() {
		String desc = getTransaction().getMemo();
		if ( desc == null || 
			 desc.trim().length() == 0 ) {
			return "";
		}
		return desc;
	}

	public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex) {
		// ::EMPTY
	}

	public String getColumnName(final int columnIndex) {
		return defaultColumnNames[columnIndex];
	}

	public void addTableModelListener(final TableModelListener l) {
		// ::EMPTY
	}

	public void removeTableModelListener(final TableModelListener l) {
		// ::EMPTY
	}

	public boolean isCellEditable(final int aRowIndex, final int aColumnIndex) {
		return false;
	}
}
