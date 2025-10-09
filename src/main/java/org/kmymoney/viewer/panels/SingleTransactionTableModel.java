package org.kmymoney.viewer.panels;

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
import org.kmymoney.viewer.models.KMyMoneyTransactionsSplitsTableModel;

/**
 * TableModel to show and edit the splits and details of a single transaction.
 */
public class SingleTransactionTableModel implements KMyMoneyTransactionsSplitsTableModel {

	/**
	 * The transaction that we are showing.
	 */
	private KMyMoneyTransaction myTransaction;

	/**
	 * The columns we display.
	 */
	private final String[] defaultColumnNames = new String[] {"date", "action", "description", "account", "+", "-"};

	/**
	 * How to format dates.
	 */
	public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern(Const.STANDARD_DATE_FORMAT);

	/**
	 * How to format currencies.
	 */
	public static final NumberFormat DEFAULT_CURRENCY_FORMAT = NumberFormat.getCurrencyInstance();

	/**
	 * @param aTransaction the transaction we are showing
	 */
	public SingleTransactionTableModel(final KMyMoneyTransaction aTransaction) {
		super();
		myTransaction = aTransaction;
	}

	/**
	 * @return true if more then 1 currency is involved
	 */
	public boolean isMultiCurrency() {
		if (getTransaction() == null) {
			return false;
		}

		for (KMyMoneyTransactionSplit split : getTransaction().getSplits()) {
			if (split.getAccount().getQualifSecCurrID().getType() != getTransaction().getQualifSecCurrID().getType()
					|| !split.getAccount().getQualifSecCurrID().equals(getTransaction().getQualifSecCurrID().toString())) {
				return true;
			}
		}

		return false;
	}

	/**
	 */
	public SingleTransactionTableModel() {
		super();
		myTransaction = null;
	}

	/**
	 * @return Returns the transaction.
	 * @see #myTransaction
	 */
	public KMyMoneyTransaction getTransaction() {
		return myTransaction;
	}

	/**
	 * @param aTransaction The transaction to set.
	 * @see #myTransaction
	 */
	public void setTransaction(final KMyMoneyTransaction aTransaction) {
		if (aTransaction == null) {
			throw new IllegalArgumentException("null 'aTransaction' given!");
		}

		Object old = myTransaction;
		if (old == aTransaction) {
			return; // nothing has changed
		}
		myTransaction = aTransaction;
	}

	/**
	 * ${@inheritDoc}.
	 */
	public KMyMoneyTransactionSplit getTransactionSplit(final int aRowIndex) {
		return getTransactionSplits().get(aRowIndex);
	}

	/**
	 * ${@inheritDoc}.
	 */
	public List<KMyMoneyTransactionSplit> getTransactionSplits() {
		KMyMoneyTransaction transaction = getTransaction();
		if (transaction == null) {
			return new LinkedList<KMyMoneyTransactionSplit>();
		}
		return new ArrayList<KMyMoneyTransactionSplit>(transaction.getSplits());

	}

	/**
	 * {@inheritDoc}
	 *
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	public int getColumnCount() {
		return defaultColumnNames.length;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public int getRowCount() {

		KMyMoneyTransaction transaction = getTransaction();
		if (transaction == null) {
			return 0;
		}
		return 1 + getTransactionSplits().size();
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see javax.swing.table.TableModel#getColumnClass(int)
	 */
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
		// "date", "action", "description", "account", "+", "-"};
		try {
			if (rowIndex == 0) {
				// show data of transaction
				switch (columnIndex) {
					case 0: // date
						return getTransaction().getDatePostedFormatted();
					case 1: // action == transaction-Number
						// ::TODO
						// return getTransactionNumber();
					case 2: // description
						return getTransactionDescription();
					case 3: { // account
						return "";
					}
					case 4: { // +
						return "";
					}
					case 5: { // -
						return "";
					}

					default:
						throw new IllegalArgumentException("illegal columnIndex " + columnIndex);
				}
			}

			KMyMoneyTransactionSplit split = getTransactionSplit(rowIndex - 1);

			switch (columnIndex) {
				case 0: { // date
					return split.getTransaction().getDatePostedFormatted();
				}
				case 1: { // action
					Action action = split.getAction();
					if (action == null) {
						return "";
					}
					return action;
				}
				case 2: { // description
					String desc = split.getMemo();
					if (desc == null || desc.trim().length() == 0) {
						return "";
					}
					return desc;
				}
				case 3: { // account
					return split.getAccount().getQualifiedName();
				}
				case 4: { // +
					if (split.getValue().isPositive()) {
						if (split.getAccount().getQualifSecCurrID().getType() == getTransaction().getQualifSecCurrID().getType()
								&& split.getAccount().getQualifSecCurrID().equals(getTransaction().getQualifSecCurrID())) {
							return split.getValueFormatted();
						}
						return split.getValueFormatted() + " (" + split.getSharesFormatted() + ")";
					} else {
						return "";
					}
				}
				case 5: { // -
					if (!split.getValue().isPositive()) {
						if (split.getAccount().getQualifSecCurrID().getType() == getTransaction().getQualifSecCurrID().getType()
								&& split.getAccount().getQualifSecCurrID().equals(getTransaction().getQualifSecCurrID())) {
							return split.getValueFormatted();
						}
						return split.getValueFormatted() + " (" + split.getSharesFormatted() + ")";
					} else {
						return "";
					}
				}

				default:
					throw new IllegalArgumentException("illegal columnIndex " + columnIndex);
			}

		}
		catch (Exception x) {

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
		if (desc == null || desc.trim().length() == 0) {
			return "";
		}
		return desc;
	}

	// ::TODO
//	/**
//	 * @return the transaction-number as we display it. Never null.
//	 */
//	private Object getTransactionNumber() {
//		String number = getTransaction().getNumber();
//		if (number == null || number.trim().length() == 0) {
//			return "";
//		}
//		return number;
//	}

	/**
	 * {@inheritDoc}
	 *
	 * @see javax.swing.table.TableModel#setValueAt(java.lang.Object, int, int)
	 */
	public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex) {
		// ignored, this model is read-only
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see javax.swing.table.TableModel#getColumnName(int)
	 */
	public String getColumnName(final int columnIndex) {
		return defaultColumnNames[columnIndex]; //TODO: l10n
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see javax.swing.table.TableModel#addTableModelListener(javax.swing.event.TableModelListener)
	 */
	public void addTableModelListener(final TableModelListener l) {
		// ignored, this model is read-only
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see javax.swing.table.TableModel#removeTableModelListener(javax.swing.event.TableModelListener)
	 */
	public void removeTableModelListener(final TableModelListener l) {
		// ignored, this model is read-only
	}

	/**
	 * @param aRowIndex    the row
	 * @param aColumnIndex the column
	 * @return false
	 */
	public boolean isCellEditable(final int aRowIndex, final int aColumnIndex) {
		return false;
	}
}
