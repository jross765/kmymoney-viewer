package org.kmymoney.jkmymoney.panels;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import javax.swing.JOptionPane;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.kmymoney.api.read.KMyMoneyAccount;
import org.kmymoney.api.read.KMyMoneyTransaction;
import org.kmymoney.api.read.KMyMoneyTransactionSplit;
import org.kmymoney.api.write.KMyMoneyWritableTransaction;
import org.kmymoney.api.write.KMyMoneyWritableTransactionSplit;
import org.kmymoney.base.basetypes.complex.KMMComplAcctID;
import org.kmymoney.base.basetypes.simple.InvalidKMMIDException;
import org.kmymoney.viewer.panels.SingleTransactionTableModel;

import xyz.schnorxoborx.base.beanbase.NoEntryFoundException;
import xyz.schnorxoborx.base.beanbase.TooManyEntriesFoundException;
import xyz.schnorxoborx.base.numbers.FixedPointNumber;

/**
 * Extended version of the {@link SingleWritableTransactionTableModel} that
 * allows changing the displayed transaction
 */
class SingleWritableTransactionTableModel extends SingleTransactionTableModel {

	/**
	 * ${@inheritDoc}.
	 */
	@Override
	public int getRowCount() {
		// add one row for adding a new split
		return super.getRowCount() + 1;
	}

	/**
	 * ${@inheritDoc}.
	 */
	@Override
	public Object getValueAt(final int aRowIndex, final int aColumnIndex) {
		if (aRowIndex == getRowCount() - 1) {
			// add one row for adding a new split
			return "";
		}
		return super.getValueAt(aRowIndex, aColumnIndex);
	}

	/**
	 * @see javax.swing.table.TableModel#addTableModelListener(javax.swing.event.TableModelListener)
	 */
	private final Set<TableModelListener> myTableModelListeners = new HashSet<TableModelListener>();

	/**
	 * @param aTransaction the transaction we are displaying.
	 */
	public SingleWritableTransactionTableModel(final KMyMoneyTransaction aTransaction) {
		super(aTransaction);
	}

	/**
	 * Create an empty model.
	 */
	public SingleWritableTransactionTableModel() {
	}

	/**
	 * @return Returns the transaction.
	 * @see #myTransaction
	 */
	public KMyMoneyWritableTransaction getWritableTransaction() {
		return (KMyMoneyWritableTransaction) super.getTransaction();
	}

	/**
	 * @param aTransaction The transaction to set.
	 * @see #myTransaction
	 */
	@Override
	public void setTransaction(final KMyMoneyTransaction aTransaction) {
		if (!(aTransaction instanceof KMyMoneyWritableTransaction)) {
			throw new IllegalArgumentException("only writable transactions allowed."
					+ " Please use ShowTransactionPanel for non-writable Transactions.");
		}
		super.setTransaction(aTransaction);
	}

	/**
	 * @param aTransaction The transaction to set.
	 * @see #myTransaction
	 */
	public void setWritableTransaction(final KMyMoneyWritableTransaction aTransaction) {
		super.setTransaction(aTransaction);
	}

	/**
	 * @param aRowIndex the split to return (starts with 0).
	 * @return the selected split of the transaction.
	 */
	public KMyMoneyWritableTransactionSplit getWritableTransactionSplit(final int aRowIndex) {
		return getWritableTransactionSplits().get(aRowIndex);
	}

	/**
	 * @return all splits of the transaction.
	 */
	public List<KMyMoneyWritableTransactionSplit> getWritableTransactionSplits() {
		KMyMoneyWritableTransaction transaction = getWritableTransaction();
		if (transaction == null) {
			return new LinkedList<>();
		}
		return new ArrayList<>(transaction.getWritableSplits());

	}

	/**
	 * {@inheritDoc}
	 *
	 * @see javax.swing.table.TableModel#setValueAt(java.lang.Object, int, int)
	 */
	@Override
	public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex) {
		if (aValue == null) {
			return;
		}
		// "date", "action", "description", "account", "+", "-"};
		try {
			if (rowIndex == 0) {
				// show data of transaction
				switch (columnIndex) {
					case 0:
						setDatePosted(aValue);
						return;
					case 1:
						// getWritableTransaction().setNumber(aValue.toString());
						return;
					case 2:
						getWritableTransaction().setMemo(aValue.toString());
						return;
					case 3:
						return;
					case 4:
						return;
					case 5:
						return;
					default:
						throw new IllegalArgumentException("illegal columnIndex " + columnIndex);
				}
			}

			KMyMoneyWritableTransactionSplit split = null;
			boolean informListeners = false;
			if (rowIndex == getRowCount() - 1) {
				// add one row for adding a new split
				if (aValue.toString().trim().length() > 0) {
					KMyMoneyWritableTransaction writableTransaction = getWritableTransaction();
					split = getWritableTransaction().createWritableSplit(getBalancingAccount(writableTransaction));
					informListeners = true;
				} else {
					return;
				}
			} else {
				split = getWritableTransactionSplit(rowIndex - 1);
			}
			if (split == null) {
				ShowWritableTransactionPanel.LOG.log(Level.SEVERE, "Split is null in setValue(row, col) - this should not be possible");
				return;
			}

			switch (columnIndex) {
				case 0:
					return;
				case 1:
					split.setAction(KMyMoneyTransactionSplit.Action.valueOf( aValue.toString()) );
					if (informListeners) {
						for (TableModelListener listener : myTableModelListeners) {
							listener.tableChanged(new TableModelEvent(this));
						}
					}
					return;
				case 2:
					split.setMemo(aValue.toString());
					if (informListeners) {
						for (TableModelListener listener : myTableModelListeners) {
							listener.tableChanged(new TableModelEvent(this));
						}
					}
					return;
				case 3:
					if (aValue.toString().trim().length() == 0
							&& split.getShares().equals(new FixedPointNumber())
							&& split.getValue().equals(new FixedPointNumber())
							&& split.getMemo().trim().length() == 0
							&& split.getAction() == null) {
						//remove split
						split.remove();
						for (TableModelListener listener : myTableModelListeners) {
							listener.tableChanged(new TableModelEvent(this));
						}
						return;
					}
					try {
						KMyMoneyAccount account = getTransaction().getKMyMoneyFile().getAccountByIDorName(new KMMComplAcctID(aValue.toString()), aValue.toString());
						if (account != null) {
							split.setAccount(account);
						}
					}
					catch (Exception e) {
						ShowWritableTransactionPanel.LOG.log(Level.SEVERE, "[Exception] Problem in "
										+ getClass().getName(),
								e);
					}
					if (informListeners) {
						for (TableModelListener listener : myTableModelListeners) {
							listener.tableChanged(new TableModelEvent(this));
						}
					}
					return;
				case 4: // retain the value in the "-" -field to sum both
					try {
						String value = aValue.toString();
						if (value.trim().length() == 0) {
							value = "0";
						}
						if (!split.getValue().isPositive()) {
							// we fill the "+"-field but there is already a value in the "-"-field -> add them
							FixedPointNumber add = split.getShares();
							FixedPointNumber addValue = split.getValue();
							int index = aValue.toString().indexOf('(');
							if (index != -1) {
								// format: "<value> (<quantity>)"
								split.setValue(value.toString().substring(0, index));
								split.setShares(value.toString().substring(index + 1).replace(')', ' '));

								split.setShares(add.add(split.getShares()));
								split.setValue(addValue.add(split.getValue()));
							} else {
								// format: "<quantity>"
								split.setShares(value.toString());
								split.setShares(add.add(split.getShares()));
							}
						} else {
							int index = value.indexOf('(');
							if (index != -1) {
								// format: "<value> (<quantity>)"
								split.setValue(value.toString().substring(0, index));
								split.setShares(value.toString().substring(index + 1).replace(')', ' '));
							} else {
								// format: "<quantity>"
								split.setShares(value.toString());
							}
						}
						if (!split.getTransaction().isBalanced()) {
							balanceTransaction();
						}
					}
					catch (Exception e) {
						ShowWritableTransactionPanel.LOG.log(Level.SEVERE, "[Exception] Problem in "
										+ getClass().getName(),
								e);
					}
					if (informListeners) {
						for (TableModelListener listener : myTableModelListeners) {
							listener.tableChanged(new TableModelEvent(this));
						}
					}
					return;
				case 5: // retain the value in the "+" -field to sum both
					try {
						String value = aValue.toString();
						if (value.trim().length() == 0) {
							value = "0";
						}
						// we fill the "-"-field but there is already a value in the "+"-field -> add them
						if (split.getValue().isPositive()) {
							FixedPointNumber sub = split.getShares();
							FixedPointNumber subValue = split.getValue();
							int index = value.toString().indexOf('(');
							if (index != -1) {
								// format: "<value> (<quantity>)"
								split.setValue(value.toString().substring(0, index));
								split.setShares(value.toString().substring(index + 1).replace(')', ' '));

								split.setShares(sub.add(split.getShares()));
								split.setValue(subValue.add(split.getValue()));
							} else {
								// format: "<quantity>"
								split.setValue(value.toString());
								split.setShares(sub.add(split.getShares()));
							}
						} else {
							int index = value.toString().indexOf('(');
							if (index != -1) {
								// format: "<value> (<quantity>)"
								split.setValue(value.toString().substring(0, index));
								split.setShares(value.toString().substring(index + 1).replace(')', ' '));
								//split.setShares(split.getShares());
								//split.setValue(split.getValue());
							} else {
								// format: "<quantity>"
								split.setValue(value.toString());
								split.setShares(split.getValue());
							}
						}
						if (!split.getTransaction().isBalanced()) {
							balanceTransaction();
						}
					}
					catch (Exception e) {
						ShowWritableTransactionPanel.LOG.log(Level.SEVERE, "[Exception] Problem in "
										+ getClass().getName(),
								e);
					}
					if (informListeners) {
						for (TableModelListener listener : myTableModelListeners) {
							listener.tableChanged(new TableModelEvent(this));
						}
					}
					return;
				default:
					throw new IllegalArgumentException("illegal columnIndex " + columnIndex);
			}

		}
		catch (Exception x) {

			String message = "Internal Error in "
					+ getClass().getName() + ":setValueAt(int rowIndex="
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
			return;
		}
	}

	/**
	 * @param aValue a new value for the DataPosted-column of the transaction
	 */
	private void setDatePosted(final Object aValue) {
		getWritableTransaction().setDatePosted(LocalDateTime.parse(aValue.toString(), DATEFORMAT).toLocalDate());
		for (TableModelListener listener : myTableModelListeners) {
			listener.tableChanged(new TableModelEvent(this));
		}
	}

	/**
	 * @throws TooManyEntriesFoundException 
	 * @throws NoEntryFoundException 
	 * @throws InvalidKMMIDException 
	 */
	protected void balanceTransaction() throws InvalidKMMIDException, NoEntryFoundException, TooManyEntriesFoundException {
		KMyMoneyWritableTransaction transaction = getWritableTransaction();
		if (transaction.isBalanced()) {
			return;
		}
		FixedPointNumber negatedBalance = transaction.getNegatedBalance();
		KMyMoneyAccount balancingAccount = getBalancingAccount(transaction);
		int i = 1;
		for (KMyMoneyWritableTransactionSplit split : transaction.getWritableSplits()) {
			i++;
			if (split.getAccountID().equals(balancingAccount.getID())) {
				split.setValue(split.getValue().add(negatedBalance));

				for (TableModelListener listener : myTableModelListeners) {
					listener.tableChanged(new TableModelEvent(this, i));
				}
				return;
			}
		}
		transaction.createWritableSplit(balancingAccount).setValue(negatedBalance);
		for (TableModelListener listener : myTableModelListeners) {
			listener.tableChanged(new TableModelEvent(this, getRowCount() - 1));
		}
	}

	/**
	 * @param transaction
	 * @return
	 * @throws TooManyEntriesFoundException 
	 * @throws NoEntryFoundException 
	 * @throws InvalidKMMIDException 
	 */
	private KMyMoneyAccount getBalancingAccount(final KMyMoneyWritableTransaction transaction) throws InvalidKMMIDException, NoEntryFoundException, TooManyEntriesFoundException {
		return transaction.getKMyMoneyFile().getAccountByIDorName(new KMMComplAcctID("xyz"), "Ausgleichskonto-EUR");
	}

	/**
	 * @see javax.swing.table.TableModel#addTableModelListener(javax.swing.event.TableModelListener)
	 */
	@Override
	public void addTableModelListener(final TableModelListener l) {
		myTableModelListeners.add(l);
	}

	/**
	 * @see javax.swing.table.TableModel#removeTableModelListener(javax.swing.event.TableModelListener)
	 */
	@Override
	public void removeTableModelListener(final TableModelListener l) {
		myTableModelListeners.remove(l);
	}

	/**
	 * @param aRowIndex    the row
	 * @param aColumnIndex the column
	 * @return true for most columns
	 */
	@Override
	public boolean isCellEditable(final int aRowIndex, final int aColumnIndex) {
		if (aRowIndex == 0) {
			// show data of transaction
			switch (aColumnIndex) {
				case 0:
					return true;
				case 1:
					return true;
				case 2:
					return true;
				case 3:
					return false;
				case 4:
					return false;
				case 5:
					return false;
				default:
					throw new IllegalArgumentException("illegal columnIndex " + aColumnIndex);
			}
		}
		return aColumnIndex > 0; // date can only be edited on the transaction. Not on the splits.
	}

}
