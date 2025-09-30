package org.kmymoney.viewer.panels;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FontMetrics;
import java.awt.Toolkit;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolTip;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import org.kmymoney.api.read.KMyMoneyAccount;
import org.kmymoney.api.read.KMyMoneyTransaction;
import org.kmymoney.api.read.KMyMoneyTransactionSplit;
import org.kmymoney.base.basetypes.complex.KMMQualifSecCurrID;
import org.kmymoney.viewer.actions.TransactionSplitAction;
import org.kmymoney.viewer.models.KMyMoneySimpleAccountTransactionsTableModel;
import org.kmymoney.viewer.models.KMyMoneyTransactionsSplitsTableModel;
import org.kmymoney.viewer.widgets.MultiLineToolTip;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.numbers.FixedPointNumber;

/**
 * This Panel shows a list of transactions.
 * These transactions currently can only be "all transactions
 * of a single account"
 * but later other transaction-lists like search-results will be possible.
 */
public class TransactionsPanel extends JPanel {

	/**
	 * Automatically created logger for debug and error-output.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(TransactionsPanel.class);

	/**
	 * for serializing.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * A scrollpane for ${@link #transactionTable}}.
	 */
	private JScrollPane transactionTableScrollPane = null;

	/**
	 * A table showing the transactions.
	 */
	private JTable transactionTable;

	/**
	 * A panel holding ${@link #selectionSummaryLabel}}.
	 */
	private JPanel selectionSummaryPanel = null;  //  @jve:decl-index=0:visual-constraint="159,213"

	/**
	 * A label showing summary-information about the selected transactions.
	 */
	private JLabel selectionSummaryLabel = null;

	/**
	 * A drop-down List to select the account the numbers
	 * in ${@link #selectionSummaryLabel}} shall refer to.
	 */
	private JComboBox selectionSummaryAccountComboBox = null;

	/**
	 * The model of our ${@link #transactionTable}.
	 */
	private KMyMoneyTransactionsSplitsTableModel model;

	/**
	 * The panel to show a single transaction.
	 */
	private ShowTransactionPanel mySingleTransactionPanel;

	/**
	 * A JPanel showing either {@link #getSelectionSummaryPanel()} or {@link #getSingleTransactionPanel()}.
	 */
	private JPanel mySummaryPanel;

	/**
	 * The actions we have on Splits.
	 */
	private Collection<TransactionSplitAction> mySplitActions;

	/**
	 * @return Returns the model.
	 * @see #model
	 */
	public KMyMoneyTransactionsSplitsTableModel getModel() {
		return model;
	}

	/**
	 * @param aModel The model to set.
	 * @see #model
	 */
	protected void setModel(final KMyMoneyTransactionsSplitsTableModel aModel) {
		if (aModel == null) {
			throw new IllegalArgumentException("null 'aModel' given!");
		}

		Object old = model;
		if (old == aModel) {
			return; // nothing has changed
		}
		model = aModel;

		getTransactionTable().setModel(model);
		getTransactionTable().setAutoCreateRowSorter(true);
		// set column-width
		FontMetrics metrics = Toolkit.getDefaultToolkit().getFontMetrics(transactionTable.getFont());
		getTransactionTable().getColumn("date").setPreferredWidth(
				SwingUtilities.computeStringWidth(metrics, KMyMoneySimpleAccountTransactionsTableModel.dateFormat.format(LocalDateTime.now())) + 5);
		getTransactionTable().getColumn("+").setPreferredWidth(
				SwingUtilities.computeStringWidth(metrics, KMyMoneySimpleAccountTransactionsTableModel.defaultCurrencyFormat.format(10000)));
		getTransactionTable().getColumn("-").setPreferredWidth(
				SwingUtilities.computeStringWidth(metrics, KMyMoneySimpleAccountTransactionsTableModel.defaultCurrencyFormat.format(-10000)));
		TableColumn balanceColumn = null;
		try {
			balanceColumn = getTransactionTable().getColumn("balance");
		}
		catch (Exception e) {
			// column is allowed to not exist
		}
		if (balanceColumn != null) {
			balanceColumn.setPreferredWidth(
					SwingUtilities.computeStringWidth(metrics, KMyMoneySimpleAccountTransactionsTableModel.defaultCurrencyFormat.format
							(-10000)));
		}

		getTransactionTable().getColumn("date").setMaxWidth(
				SwingUtilities.computeStringWidth(metrics, KMyMoneySimpleAccountTransactionsTableModel.dateFormat.format(LocalDateTime.now())) + 5);
		getTransactionTable().getColumn("+").setMaxWidth(
				SwingUtilities.computeStringWidth(metrics, KMyMoneySimpleAccountTransactionsTableModel.defaultCurrencyFormat.format(1000000)));
		getTransactionTable().getColumn("-").setMaxWidth(
				SwingUtilities.computeStringWidth(metrics, KMyMoneySimpleAccountTransactionsTableModel.defaultCurrencyFormat.format(-1000000)));
		if (balanceColumn != null) {
			balanceColumn.setMaxWidth(SwingUtilities
					.computeStringWidth(metrics, KMyMoneySimpleAccountTransactionsTableModel.defaultCurrencyFormat.format(-1000000)));
		}

		getTransactionTable().getColumn("transaction").setCellRenderer(new DesriptionCellRenderer());
		getTransactionTable().getColumn("description").setCellRenderer(new DesriptionCellRenderer());

		updateSelectionSummaryAccountList();
		updateSelectionSummary();
		getSingleTransactionPanel().setTransaction(null);

	}

	/**
	 * This is the default constructor.
	 */
	public TransactionsPanel() {
		super();
		initialize();
	}

	/**
	 * Give an account who's transactions to display.
	 *
	 * @param account if null, an empty table will be shown.
	 */
	public void setAccount(final KMyMoneyAccount account) {

		if (account == null) {
			setModel(new KMyMoneySimpleAccountTransactionsTableModel());
		} else {
			setModel(new KMyMoneySimpleAccountTransactionsTableModel(account));
		}
	}

	/**
	 * This method initializes this panel.
	 */
	private void initialize() {
		this.setSize(300, 200);
		this.setLayout(new BorderLayout());
		this.add(getTransactionTableScrollPane(), BorderLayout.CENTER);
		this.add(getSummaryPanel(), BorderLayout.SOUTH);
	}

	/**
	 * @return a JPanel showing either {@link #getSelectionSummaryPanel()} or {@link #getSingleTransactionPanel()}.
	 */
	private JPanel getSummaryPanel() {
		if (mySummaryPanel == null) {
			mySummaryPanel = new JPanel();
			mySummaryPanel.setLayout(new CardLayout());
			mySummaryPanel.add(getSelectionSummaryPanel(), "multi");
			getSingleTransactionPanel().setVisible(false);
			mySummaryPanel.add(getSingleTransactionPanel(), "single");
			getSummaryPanel().setPreferredSize(getSelectionSummaryPanel().getPreferredSize());
		}

		return mySummaryPanel;
	}

	/**
	 * This method initializes transactionTableScrollPane
	 *
	 * @return javax.swing.JScrollPane
	 */
	private JScrollPane getTransactionTableScrollPane() {
		if (transactionTableScrollPane == null) {
			transactionTableScrollPane = new JScrollPane();
			transactionTableScrollPane.setViewportView(getTransactionTable());
		}
		return transactionTableScrollPane;
	}

	/**
	 * This method initializes transactionTable.
	 *
	 * @return javax.swing.JTable
	 */
	protected JTable getTransactionTable() {
		if (transactionTable == null) {
			transactionTable = new JTable() {

				/**
				 * Our TransactionsPanel.java.
				 * @see long
				 */
				private static final long serialVersionUID = 1L;

				/**
				 * @see javax.swing.JTable#getToolTipText(java.awt.event.MouseEvent)
				 */

				@Override
				public String getToolTipText(final MouseEvent event) {
					java.awt.Point p = event.getPoint();
					int rowIndex = rowAtPoint(p);
					// convertColumnIndexToModel is needed,
					// because the user may reorder columns
					//int realColumnIndex = convertColumnIndexToModel(columnAtPoint(p));
					if (rowIndex >= 0) {

						KMyMoneySimpleAccountTransactionsTableModel model = (KMyMoneySimpleAccountTransactionsTableModel) getModel();
						KMyMoneyTransactionSplit localSplit = model.getTransactionSplit(rowIndex);
						KMyMoneyTransaction transaction = localSplit.getTransaction();
						StringBuilder output = new StringBuilder();
						output.append("\"")
								// ::TODO
								// .append(transaction.getNumber())
								.append("\t \"")
								.append(transaction.getMemo())
								.append("\"\t [")
								.append(localSplit.getAccount().getQualifiedName())
								.append("]\t ")
								.append(localSplit.getShares())
								.append(localSplit.getAccount().getQualifSecCurrID().getType() == KMMQualifSecCurrID.Type.CURRENCY ? " "
										: " x ")
								.append(localSplit.getAccount().getQualifSecCurrID())
								.append("\n");

						for (KMyMoneyTransactionSplit split : transaction.getSplits()) {
							output.append("\"")
									.append(split.getAction())
									.append("\"\t \"")
									.append(split.getMemo())
									.append("\"\t [")
									.append(split.getAccount().getQualifiedName())
									.append("]\t ")
									.append(split.getShares())
									.append(localSplit.getAccount().getQualifSecCurrID().getType() == KMMQualifSecCurrID.Type.CURRENCY
											? " " : " x ")
									.append(split.getAccount().getQualifSecCurrID())
									.append("\n");
						}
						if (!transaction.isBalanced()) {
							output.append("TRANSACTION IS NOT BALANACED! missung=" + transaction.getBalanceFormatted());
						}

						return output.toString();
					}

					// show default-tooltip
					return super.getToolTipText(event);
				}

				@Override
				public JToolTip createToolTip() {
					MultiLineToolTip tip = new MultiLineToolTip();
					tip.setComponent(this);
					return tip;
				}

			};

			// add a listener to call updateSelectionSummary() every time
			// the user changes the selected rows.
			transactionTable.getSelectionModel().addListSelectionListener(
					new ListSelectionListener() {
						public void valueChanged(
								final javax.swing.event.ListSelectionEvent e) {
							try {
								updateSelectionSummaryAccountList();
								updateSelectionSummary();
								if (getTransactionTable().getSelectedRowCount() == 1) {
									KMyMoneyTransactionSplit transactionSplit = model.getTransactionSplit(getTransactionTable().getSelectedRow
											());
									//                               setTransaction(transactionSplit.getTransaction());

									getSingleTransactionPanel().setTransaction(transactionSplit.getTransaction());
									getSingleTransactionPanel().setVisible(true);
									getSelectionSummaryPanel().setVisible(false);
									getSummaryPanel().setPreferredSize(getSingleTransactionPanel().getPreferredSize());
									//((CardLayout) getSelectionSummaryPanel().getLayout()).next(getSummaryPanel());
								} else {
									getSingleTransactionPanel().setVisible(false);
									getSingleTransactionPanel().setTransaction(null);
									getSelectionSummaryPanel().setVisible(true);
									getSummaryPanel().setPreferredSize(getSelectionSummaryPanel().getPreferredSize());
									//((CardLayout) getSelectionSummaryPanel().getLayout()).next(getSummaryPanel());
								}
							}
							catch (Exception e1) {
								LOGGER.error("", e1);
							}
						}

						;
					});
			setModel(new KMyMoneySimpleAccountTransactionsTableModel());
		}
		return transactionTable;
	}

	/**
	 * This method initializes selectionSummaryLabel.
	 *
	 * @return JLabel
	 */
	private JLabel getSelectionSummaryLabel() {
		if (selectionSummaryLabel == null) {
			selectionSummaryLabel = new JLabel();
			selectionSummaryLabel.setText("");
		}
		return selectionSummaryLabel;
	}

	/**
	 * This method initializes selectionSummaryLabel.
	 *
	 * @return JLabel
	 */
	private JComboBox getSelectionSummaryAccountComboBox() {
		if (selectionSummaryAccountComboBox == null) {
			selectionSummaryAccountComboBox = new JComboBox();
			selectionSummaryAccountComboBox.setEditable(false);
			selectionSummaryAccountComboBox.addItemListener(new ItemListener() {
				public void itemStateChanged(final java.awt.event.ItemEvent e) {
					updateSelectionSummary();
				}

				;
			});
		}
		return selectionSummaryAccountComboBox;
	}

	/**
	 * This method initializes selectionSummaryPanel.
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getSelectionSummaryPanel() {
		if (selectionSummaryPanel == null) {
			selectionSummaryPanel = new JPanel();
			selectionSummaryPanel.setLayout(new BorderLayout());
			selectionSummaryPanel.add(getSelectionSummaryLabel(),
					BorderLayout.CENTER);
			selectionSummaryPanel.add(getSelectionSummaryAccountComboBox(),
					BorderLayout.SOUTH);
		}
		return selectionSummaryPanel;
	}

	/**
	 * This method initializes ShowTransactionPanel.
	 *
	 * @return javax.swing.JPanel
	 */
	protected ShowTransactionPanel getSingleTransactionPanel() {
		if (mySingleTransactionPanel == null) {
			mySingleTransactionPanel = new ShowTransactionPanel();
			mySingleTransactionPanel.setSplitActions(getSplitActions());
		}
		return mySingleTransactionPanel;
	}

	/**
	 * Put all accounts into the model of
	 * the selectionSummaryAccountComboBox.
	 */
	private void updateSelectionSummaryAccountList() {

		Set<KMyMoneyAccount> accounts = new TreeSet<KMyMoneyAccount>();

		int selectedCount = getTransactionTable().getSelectedRowCount();
		if (selectedCount < 1) {
			// get all accounts of all splits
			int count = model.getRowCount();

			for (int i = 0; i < count; i++) {
				KMyMoneyTransactionSplit transactionSplit =
						model.getTransactionSplit(i);

				try {
					KMyMoneyTransaction transaction = transactionSplit.getTransaction();
					if (transaction == null) {
						LOGGER.error("Split has no transaction");
					} else {
						Collection<? extends KMyMoneyTransactionSplit> splits =
								transaction.getSplits();
						for (KMyMoneyTransactionSplit split : splits) {
							try {
								KMyMoneyAccount account = split.getAccount();
								if (account != null) {
									if (!accounts.contains(account)) {
										accounts.add(account);
									}
								}
							}
							catch (Exception x) {
								System.err.println("Ignoring account in "
										+ "TransactionPanel::updateSelectionSummary"
										+ "AccountList() because of:");
								x.printStackTrace(System.err);
							}
						}
					}
				}
				catch (Exception e) {
					LOGGER.error("Problem in "
									+ getClass().getName(),
							e);
				}
			}
		} else {
			// show a summary only for the selected transactions
			int[] selectedRows = getTransactionTable().getSelectedRows();

			for (int selectedRow : selectedRows) {
				KMyMoneyTransactionSplit transactionSplit = model.getTransactionSplit(selectedRow);
				Collection<? extends KMyMoneyTransactionSplit> splits = transactionSplit.getTransaction().getSplits();
				for (KMyMoneyTransactionSplit split : splits) {
					if (!accounts.contains(split.getAccount())) {
						accounts.add(split.getAccount());
					}
				}

			}
		}

		DefaultComboBoxModel aModel = new DefaultComboBoxModel();
		for (KMyMoneyAccount account : accounts) {
			aModel.addElement(account);
		}

		JComboBox list = getSelectionSummaryAccountComboBox();
		list.setModel(aModel);
		list.setSelectedIndex(-1);
	}

	/**
	 * If the user has selected an account to display a summary for,
	 * we add all splits of the given split's transaction that have
	 * that account. If nothing is selected, we add the given split.
	 *
	 * @param retval the list of splits to add to
	 * @param split  the split who's transaction to look at
	 */
	private void replaceSplitsWithSelectedAccountsSplits(final Set<KMyMoneyTransactionSplit> retval, final KMyMoneyTransactionSplit split) {

		JComboBox combo = getSelectionSummaryAccountComboBox();
		KMyMoneyAccount selectedAccount =
				(KMyMoneyAccount) combo.getSelectedItem();
		if (selectedAccount == null) {
			retval.add(split);
		} else {
			KMyMoneyTransaction transaction = split.getTransaction();
			for (KMyMoneyTransactionSplit split2 : transaction.getSplits()) {
				if (split2 == null) {
					continue;
				}
				KMyMoneyAccount account = split2.getAccount();
				if (account != null && account.equals(selectedAccount)) {
					retval.add(split2);
				}
			}
		}

	}

	/**
	 * If the user has selected an account to display a summary for,
	 * we return All splits of the selected transactions that are
	 * for that account. Else we return all selected Splits.
	 * If no Splits are selected, we use all splits as the selected
	 * ones.
	 *
	 * @return the splits of the selected/all transactions featuring this/the
	 * selected account.
	 */
	private Collection<KMyMoneyTransactionSplit> getSplitsForSummary() {
		int[] selectedRows = getTransactionTable().getSelectedRows();
		Set<KMyMoneyTransactionSplit> retval
				= new HashSet<KMyMoneyTransactionSplit>();

		if (selectedRows == null || selectedRows.length == 0) {
			int count = model.getRowCount();
			for (int i = 0; i < count; i++) {
				KMyMoneyTransactionSplit transactionSplit
						= model.getTransactionSplit(i);
				replaceSplitsWithSelectedAccountsSplits(retval,
						transactionSplit);
			}
		} else {
			for (int selectedRow : selectedRows) {
				KMyMoneyTransactionSplit transactionSplit
						= model.getTransactionSplit(selectedRow);
				replaceSplitsWithSelectedAccountsSplits(retval,
						transactionSplit);
			}
		}

		return retval;
	}

	/**
	 * Update the text in the selectionSummaryLabel
	 * to show summary-information about the currently
	 * selected transactions.
	 */
	private void updateSelectionSummary() {

		int selectedCount = getTransactionTable().getSelectedRowCount();
		FixedPointNumber valueSumPlus = new FixedPointNumber(0);
		FixedPointNumber valueSumMinus = new FixedPointNumber(0);
		FixedPointNumber valueSumBalance = new FixedPointNumber(0);
		NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();

		Collection<KMyMoneyTransactionSplit> splits;
		splits = getSplitsForSummary();
		for (KMyMoneyTransactionSplit transactionSplit : splits) {
			FixedPointNumber value = transactionSplit.getValue();
			valueSumBalance.add(value);
			if (value.isPositive()) {
				valueSumPlus.add(value);
			} else {
				valueSumMinus.add(value);
			}
		}

		if (selectedCount < 1) {
			// show a summary for all transactions displayed
			int count = model.getRowCount();
			getSelectionSummaryLabel().setText(count + " transactions "
					+ currencyFormat.format(valueSumPlus)
					+ currencyFormat.format(valueSumMinus)
					+ "=" + currencyFormat.format(valueSumBalance));
		} else {
			// show a summary only for the selected transactions
			getSelectionSummaryLabel().setText(selectedCount
					+ " transactions selected "
					+ currencyFormat.format(valueSumPlus)
					+ currencyFormat.format(valueSumMinus)
					+ "=" + currencyFormat.format(valueSumBalance));
		}

	}

	/**
	 * @param aTransaction the transactions to show in detail
	 */
	public void setTransaction(final KMyMoneyTransaction aTransaction) {
		TableModel temp = getTransactionTable().getModel();
		if (temp != null && temp instanceof KMyMoneyTransactionsSplitsTableModel) {
			KMyMoneyTransactionsSplitsTableModel tblModel = (KMyMoneyTransactionsSplitsTableModel) temp;
			int max = tblModel.getRowCount();
			for (int i = 0; i < max; i++) {
				if (tblModel.getTransactionSplit(i).getTransaction().getID().equals( aTransaction.getID() ) ) {
					getTransactionTable().getSelectionModel().setSelectionInterval(i, i);
					return;
				}
			}
		}
		getSingleTransactionPanel().setTransaction(aTransaction);
		getSingleTransactionPanel().setVisible(true);
		getSelectionSummaryPanel().setVisible(false);
		getSummaryPanel().setPreferredSize(getSingleTransactionPanel().getPreferredSize());
	}

	/**
	 * @param aSplitActions the actions we shall offer on splits.
	 */
	public void setSplitActions(final Collection<TransactionSplitAction> aSplitActions) {
		LOGGER.info("TransactionsPanel is given " + (mySplitActions == null ? "no" : mySplitActions.size()) + " split-actions");
		mySplitActions = aSplitActions;
		getSingleTransactionPanel().setSplitActions(mySplitActions);
	}

	/**
	 * @return the actions we shall offer on splits.
	 */
	protected Collection<TransactionSplitAction> getSplitActions() {
		return mySplitActions;
	}
}
