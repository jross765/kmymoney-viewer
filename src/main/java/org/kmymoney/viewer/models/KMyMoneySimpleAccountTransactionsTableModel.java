package org.kmymoney.viewer.models;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Currency;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.event.TableModelListener;

import org.kmymoney.api.read.KMyMoneyAccount;
import org.kmymoney.api.read.KMyMoneyTransactionSplit;
import org.kmymoney.base.basetypes.complex.KMMQualifSecCurrID;

/**
 * A TableModel that shows the transaction and balance of an Account.
 */
public class KMyMoneySimpleAccountTransactionsTableModel implements KMyMoneyTransactionsSplitsTableModel {

	/**
	 * The account who's transactions we are showing.
	 */
	private final KMyMoneyAccount account;

	/**
	 * The columns we display.
	 */
	private final String[] defaultColumnNames = new String[] {"date", "transaction", "description", "+", "-", "balance"};

	/**
	 * @param anAccount the account whos splits to display.
	 */
	public KMyMoneySimpleAccountTransactionsTableModel(final KMyMoneyAccount anAccount) {
		super();
		account = anAccount;
	}

	/**
	 * the Table will be empty.
	 *
	 */
	public KMyMoneySimpleAccountTransactionsTableModel() {
		super();
		account = null;
	}

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
		if (transactionSplits == null) {
			return 0;
		}
		return transactionSplits.size();
	}

	/**
	 * @return the splits that affect this account.
	 */
	public List<? extends KMyMoneyTransactionSplit> getTransactionSplits() {
		if (account == null) {
			return new LinkedList<KMyMoneyTransactionSplit>();
		}
		return account.getTransactionSplits();
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
	 * How to format dates.
	 */
	public static final DateTimeFormatter dateFormat = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
	/**
	 * How to format currencies.
	 */
	private NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
	/**
	 * How to format currencies.
	 */
	public static final NumberFormat defaultCurrencyFormat = NumberFormat.getCurrencyInstance();

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

			updateCurrencyFormat(split);

			switch (columnIndex) {
				case 0: { //DATE
					return dateFormat.format(split.getTransaction().getDatePosted());
				}
				case 1: { //transaction
					String desc = split.getTransaction().getMemo();
					if (desc == null || desc.trim().length() == 0) {
						return "";
					}
					return desc;
				}
				case 2: { //description
					String desc = split.getMemo();
					if (desc == null || desc.trim().length() == 0) {
						return "";
					}
					return desc;
				}
				case 3: { // +
					if (split.getShares().isPositive()) {
						//                  //T O D O: use default-currency here
						//                  if (account != null && !account.getCurrencyID().equals("EUR")) {
						//                      return split.getValueFormatet();
						//                  }
						return currencyFormat.format(split.getShares());
					} else {
						return "";
					}
				}
				case 4: { // -
					if (!split.getShares().isPositive()) {
						//                    if (account != null && !account.getCurrencyID().equals("EUR")) {
						//                        return split.getValueFormatet();
						//                    }
						return currencyFormat.format(split.getShares());
					} else {
						return "";
					}
				}
				case 5: { // balance
					if (account != null) {
						return currencyFormat.format(account.getBalance(split));
					} else {
						return currencyFormat.format(split.getAccount().getBalance(split));
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
	 * @param split the split whos account to use for the currency
	 */
	private void updateCurrencyFormat(final KMyMoneyTransactionSplit split) {
		currencyFormat = NumberFormat.getNumberInstance();
		try {
			if (split.getAccount().getQualifSecCurrID().getType() == KMMQualifSecCurrID.Type.CURRENCY) {
				Currency currency = Currency.getInstance(split.getAccount().getQualifSecCurrID().toString());
				currencyFormat = NumberFormat.getCurrencyInstance();
				currencyFormat.setCurrency(currency);
			}
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex) {

	}

	/**
	 * {@inheritDoc}
	 */
	public String getColumnName(final int columnIndex) {
		return defaultColumnNames[columnIndex]; //TODO: l10n
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
