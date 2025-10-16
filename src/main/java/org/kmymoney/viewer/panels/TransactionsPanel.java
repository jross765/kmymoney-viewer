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
import org.kmymoney.viewer.Const;
import org.kmymoney.viewer.actions.TransactionSplitAction;
import org.kmymoney.viewer.models.KMyMoneySimpleAccountTransactionsTableModel;
import org.kmymoney.viewer.models.KMyMoneyTransactionSplitsTableModel;
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

	private static final Logger LOGGER = LoggerFactory.getLogger(TransactionsPanel.class);

	private static final int DEFAULT_WIDTH = 300;
	private static final int DEFAULT_HEIGHT = 200;

	// For serializing
	private static final long serialVersionUID = 1L;

	// A scrollpane for ${@link #transactionTable}}
	private JScrollPane trxTabScrollPane = null;

	// A table showing the transactions
	private JTable trxTab;

	// A panel holding ${@link #selectionSummaryLabel}}
	private JPanel selectionSummaryPanel = null;

	// A label showing summary-information about the selected transactions.
	private JLabel selectionSummaryLabel = null;

	// A drop-down List to select the account the numbers
	// in ${@link #selectionSummaryLabel}} shall refer to.
	private JComboBox selectionSummaryAccountComboBox = null;

	// The model of our ${@link #transactionTable}
	private KMyMoneyTransactionSplitsTableModel model;

	// The panel to show a single transaction
	private ShowTransactionPanel mySingleTransactionPanel;

	// A JPanel showing either {@link #getSelectionSummaryPanel()} or {@link #getSingleTransactionPanel()}
	private JPanel mySummaryPanel;

	// The actions we have on Splits
	private Collection<TransactionSplitAction> mySplitActions;

	/**
	 * @return Returns the model
	 * @see #model
	 */
	public KMyMoneyTransactionSplitsTableModel getModel() {
		return model;
	}

	/**
	 * @param aModel The model to set.
	 * @see #model
	 */
	protected void setModel(final KMyMoneyTransactionSplitsTableModel aModel) {
		if ( aModel == null ) {
			throw new IllegalArgumentException("argument <aModel> is null"); //$NON-NLS-1$
		}

		Object old = model;
		if (old == aModel) {
			return; // nothing has changed
		}
		model = aModel;

		getTransactionTable().setModel(model);
		getTransactionTable().setAutoCreateRowSorter(true);

		TableColumn balanceColumn = null;
		try {
			balanceColumn = getTransactionTable().getColumn(Messages_TransactionsPanel.getString("TransactionsPanel.1")); //$NON-NLS-1$
		} catch (Exception e) {
			// column is allowed not to exist
		}
		
		// ---
		// BEGIN col widths
		FontMetrics metrics = Toolkit.getDefaultToolkit().getFontMetrics(trxTab.getFont());
		
		int currencyWidthDefault = SwingUtilities.computeStringWidth(metrics, KMyMoneySimpleAccountTransactionsTableModel.DEFAULT_CURRENCY_FORMAT.format(Const.TABLE_COL_AMOUNT_WIDTH_VAL_SMALL));
		int currencyWidthMax     = SwingUtilities.computeStringWidth(metrics, KMyMoneySimpleAccountTransactionsTableModel.DEFAULT_CURRENCY_FORMAT.format(Const.TABLE_COL_AMOUNT_WIDTH_VAL_BIG));

		getTransactionTable().getColumn(Messages_TransactionsPanel.getString("TransactionsPanel.2")).setPreferredWidth( //$NON-NLS-1$
				SwingUtilities.computeStringWidth(metrics, KMyMoneySimpleAccountTransactionsTableModel.DATE_FORMAT.format(LocalDateTime.now())) + Const.TABLE_COL_EXTRA_WIDTH);
//		getTransactionTable().getColumn(Messages_TransactionsPanel.getString("TransactionsPanel.6")).setPreferredWidth(Const.PANEL_DEFAULT_WIDTH); //$NON-NLS-1$
//		getTransactionTable().getColumn(Messages_TransactionsPanel.getString("TransactionsPanel.7")).setPreferredWidth(Const.PANEL_DEFAULT_WIDTH); //$NON-NLS-1$
		getTransactionTable().getColumn(Messages_TransactionsPanel.getString("TransactionsPanel.3")).setPreferredWidth(currencyWidthDefault); //$NON-NLS-1$
		getTransactionTable().getColumn(Messages_TransactionsPanel.getString("TransactionsPanel.4")).setPreferredWidth(currencyWidthDefault); //$NON-NLS-1$
		if (balanceColumn != null) {
			balanceColumn.setPreferredWidth(currencyWidthDefault);
		}

		getTransactionTable().getColumn(Messages_TransactionsPanel.getString("TransactionsPanel.2")).setMinWidth(Const.TABLE_COL_MIN_WIDTH); //$NON-NLS-1$
		getTransactionTable().getColumn(Messages_TransactionsPanel.getString("TransactionsPanel.6")).setMinWidth(Const.TABLE_COL_MIN_WIDTH); //$NON-NLS-1$
		getTransactionTable().getColumn(Messages_TransactionsPanel.getString("TransactionsPanel.7")).setMinWidth(Const.TABLE_COL_MIN_WIDTH); //$NON-NLS-1$
		getTransactionTable().getColumn(Messages_TransactionsPanel.getString("TransactionsPanel.3")).setMinWidth(Const.TABLE_COL_MIN_WIDTH); //$NON-NLS-1$
		getTransactionTable().getColumn(Messages_TransactionsPanel.getString("TransactionsPanel.4")).setMinWidth(Const.TABLE_COL_MIN_WIDTH); //$NON-NLS-1$
		if (balanceColumn != null) {
			balanceColumn.setMinWidth(Const.TABLE_COL_MIN_WIDTH);
		}

//		getTransactionTable().getColumn(Messages_TransactionsPanel.getString("TransactionsPanel.2")).setMaxWidth(
//				SwingUtilities.computeStringWidth(metrics, KMyMoneySimpleAccountTransactionsTableModel.dateFormat.format(LocalDateTime.now())) + Const.TABLE_COL_WIDTH_TOL);
		getTransactionTable().getColumn(Messages_TransactionsPanel.getString("TransactionsPanel.2")).setMaxWidth(Const.TABLE_COL_MAX_WIDTH); //$NON-NLS-1$
//		getTransactionTable().getColumn(Messages_TransactionsPanel.getString("TransactionsPanel.6")).setMaxWidth(Const.PANEL_MAX_WIDTH); //$NON-NLS-1$
//		getTransactionTable().getColumn(Messages_TransactionsPanel.getString("TransactionsPanel.7")).setMaxWidth(Const.PANEL_MAX_WIDTH); //$NON-NLS-1$
		getTransactionTable().getColumn(Messages_TransactionsPanel.getString("TransactionsPanel.3")).setMaxWidth(currencyWidthMax); //$NON-NLS-1$
		getTransactionTable().getColumn(Messages_TransactionsPanel.getString("TransactionsPanel.4")).setMaxWidth(currencyWidthMax); //$NON-NLS-1$
		if (balanceColumn != null) {
			balanceColumn.setMaxWidth(currencyWidthMax);
		}
		// END col widths
		// ---
		
		getTransactionTable().getColumn(Messages_TransactionsPanel.getString("TransactionsPanel.6")).setCellRenderer(new DesriptionCellRenderer()); //$NON-NLS-1$
		getTransactionTable().getColumn(Messages_TransactionsPanel.getString("TransactionsPanel.7")).setCellRenderer(new DesriptionCellRenderer()); //$NON-NLS-1$

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
		if ( account == null ) {
			setModel(new KMyMoneySimpleAccountTransactionsTableModel());
		} else {
			setModel(new KMyMoneySimpleAccountTransactionsTableModel(account));
		}
	}

	/**
	 * This method initializes this panel.
	 */
	private void initialize() {
		this.setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
		this.setLayout(new BorderLayout());
		this.add(getTransactionTableScrollPane(), BorderLayout.CENTER);
		this.add(getSummaryPanel(), BorderLayout.SOUTH);
	}

	/**
	 * @return a JPanel showing either {@link #getSelectionSummaryPanel()} or {@link #getSingleTransactionPanel()}.
	 */
	private JPanel getSummaryPanel() {
		if ( mySummaryPanel == null ) {
			mySummaryPanel = new JPanel();
			mySummaryPanel.setLayout(new CardLayout());
			mySummaryPanel.add(getSelectionSummaryPanel(), Messages_TransactionsPanel.getString("TransactionsPanel.15")); //$NON-NLS-1$
			getSingleTransactionPanel().setVisible(false);
			mySummaryPanel.add(getSingleTransactionPanel(), Messages_TransactionsPanel.getString("TransactionsPanel.16")); //$NON-NLS-1$
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
		if ( trxTabScrollPane == null ) {
			trxTabScrollPane = new JScrollPane();
			trxTabScrollPane.setViewportView(getTransactionTable());
		}
		
		return trxTabScrollPane;
	}

	/**
	 * This method initializes transactionTable.
	 *
	 * @return javax.swing.JTable
	 */
	protected JTable getTransactionTable() {
		if ( trxTab == null ) {
			trxTab = new JTable() {

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
					if ( rowIndex >= 0 ) {
						KMyMoneySimpleAccountTransactionsTableModel model = (KMyMoneySimpleAccountTransactionsTableModel) getModel();
						KMyMoneyTransactionSplit localSplit = model.getTransactionSplit(rowIndex);
						KMyMoneyTransaction trx = localSplit.getTransaction();
						StringBuilder output = new StringBuilder();
						output.append(trx.toString());

						if ( ! trx.isBalanced() ) {
							output.append(" TRANSACTION IS NOT BALANCED! missing=" + trx.getBalanceFormatted()); //$NON-NLS-1$
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
			trxTab.getSelectionModel().addListSelectionListener(
					new ListSelectionListener() {
						public void valueChanged(
								final javax.swing.event.ListSelectionEvent e) {
							try {
								updateSelectionSummaryAccountList();
								updateSelectionSummary();
								if ( getTransactionTable().getSelectedRowCount() == 1 ) {
									KMyMoneyTransactionSplit splt = model.getTransactionSplit(getTransactionTable().getSelectedRow());
									//                               setTransaction(transactionSplit.getTransaction());

									getSingleTransactionPanel().setTransaction(splt.getTransaction());
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
								LOGGER.error("", e1); //$NON-NLS-1$
							}
						}

						;
					});
			
			setModel(new KMyMoneySimpleAccountTransactionsTableModel());
		}
		
		return trxTab;
	}

	/**
	 * This method initializes selectionSummaryLabel.
	 *
	 * @return JLabel
	 */
	private JLabel getSelectionSummaryLabel() {
		if ( selectionSummaryLabel == null ) {
			selectionSummaryLabel = new JLabel();
			selectionSummaryLabel.setText(""); //$NON-NLS-1$
		}
		
		return selectionSummaryLabel;
	}

	/**
	 * This method initializes selectionSummaryLabel.
	 *
	 * @return JLabel
	 */
	private JComboBox getSelectionSummaryAccountComboBox() {
		if ( selectionSummaryAccountComboBox == null ) {
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
		if ( selectionSummaryPanel == null ) {
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
		if ( mySingleTransactionPanel == null ) {
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
		if ( selectedCount < 1 ) {
			// get all accounts of all splits
			int count = model.getRowCount();

			for ( int i = 0; i < count; i++ ) {
				KMyMoneyTransactionSplit transactionSplit = model.getTransactionSplit(i);

				try {
					KMyMoneyTransaction trx = transactionSplit.getTransaction();
					if ( trx == null ) {
						LOGGER.error("updateSelectionSummaryAccountList: Split has no transaction"); //$NON-NLS-1$
					} else {
						Collection<? extends KMyMoneyTransactionSplit> splits = trx.getSplits();
						for ( KMyMoneyTransactionSplit splt : splits ) {
							try {
								KMyMoneyAccount acct = splt.getAccount();
								if ( acct != null ) {
									if ( ! accounts.contains(acct) ) {
										accounts.add(acct);
									}
								}
							}
							catch (Exception x) {
								System.err.println("Ignoring account in " //$NON-NLS-1$
										+ "TransactionPanel::updateSelectionSummary" //$NON-NLS-1$
										+ "AccountList() because of:"); //$NON-NLS-1$
								x.printStackTrace(System.err);
							}
						}
					}
				}
				catch (Exception e) {
					LOGGER.error("updateSelectionSummaryAccountList: Problem in " //$NON-NLS-1$
									+ getClass().getName(),
							e);
				}
			}
		} else {
			// show a summary only for the selected transactions
			int[] selectedRows = getTransactionTable().getSelectedRows();

			for ( int selectedRow : selectedRows ) {
				KMyMoneyTransactionSplit splt = model.getTransactionSplit(selectedRow);
				Collection<? extends KMyMoneyTransactionSplit> splits = splt.getTransaction().getSplits();
				for ( KMyMoneyTransactionSplit splt2 : splits ) {
					if ( ! accounts.contains(splt2.getAccount()) ) {
						accounts.add(splt2.getAccount());
					}
				}

			}
		}

		DefaultComboBoxModel aModel = new DefaultComboBoxModel();
		for ( KMyMoneyAccount acct : accounts ) {
			aModel.addElement(acct);
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
	 * @param splt  the split who's transaction to look at
	 */
	private void replaceSplitsWithSelectedAccountsSplits(final Set<KMyMoneyTransactionSplit> retval, final KMyMoneyTransactionSplit splt) {
		JComboBox combo = getSelectionSummaryAccountComboBox();
		KMyMoneyAccount selectedAcct = (KMyMoneyAccount) combo.getSelectedItem();
		
		if ( selectedAcct == null ) {
			retval.add(splt);
		} else {
			KMyMoneyTransaction trx = splt.getTransaction();
			for ( KMyMoneyTransactionSplit splt2 : trx.getSplits() ) {
				if ( splt2 == null ) {
					continue;
				}
				KMyMoneyAccount acct = splt2.getAccount();
				if ( acct != null && 
					 acct.equals(selectedAcct) ) {
					retval.add(splt2);
				}
			}
		}

	}

	/**
	 * If the user has selected an account to display a summary for,
	 * we return all splits of the selected transactions that belong
	 * to that account. Else, we return all selected Splits.
	 * If no Splits are selected, we use all splits as the selected
	 * ones.
	 *
	 * @return the splits of the selected/all transactions featuring this/the
	 * selected account.
	 */
	private Collection<KMyMoneyTransactionSplit> getSplitsForSummary() {
		int[] selectedRows = getTransactionTable().getSelectedRows();
		Set<KMyMoneyTransactionSplit> retval = new HashSet<KMyMoneyTransactionSplit>();

		if ( selectedRows == null || 
			 selectedRows.length == 0 ) {
			int count = model.getRowCount();
			for ( int i = 0; i < count; i++ ) {
				KMyMoneyTransactionSplit splt = model.getTransactionSplit(i);
				replaceSplitsWithSelectedAccountsSplits(retval, splt);
			}
		} else {
			for ( int selectedRow : selectedRows ) {
				KMyMoneyTransactionSplit splt = model.getTransactionSplit(selectedRow);
				replaceSplitsWithSelectedAccountsSplits(retval, splt);
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
		for ( KMyMoneyTransactionSplit splt : splits ) {
			FixedPointNumber value = splt.getValue();
			valueSumBalance.add(value);
			if ( value.isPositive() ) {
				valueSumPlus.add(value);
			} else {
				valueSumMinus.add(value);
			}
		}

		if ( selectedCount < 1 ) {
			// show a summary for all transactions displayed
			int count = model.getRowCount();
			getSelectionSummaryLabel().setText(count + Messages_TransactionsPanel.getString("TransactionsPanel.39") //$NON-NLS-1$
					+ currencyFormat.format(valueSumPlus)
					+ currencyFormat.format(valueSumMinus)
					+ "=" + currencyFormat.format(valueSumBalance)); //$NON-NLS-1$
		} else {
			// show a summary only for the selected transactions
			getSelectionSummaryLabel().setText(selectedCount
					+ Messages_TransactionsPanel.getString("TransactionsPanel.41") //$NON-NLS-1$
					+ currencyFormat.format(valueSumPlus)
					+ currencyFormat.format(valueSumMinus)
					+ "=" + currencyFormat.format(valueSumBalance)); //$NON-NLS-1$
		}

	}

	/**
	 * @param trx the transactions to show in detail
	 */
	public void setTransaction(final KMyMoneyTransaction trx) {
		TableModel temp = getTransactionTable().getModel();
		if ( temp != null && 
			 temp instanceof KMyMoneyTransactionSplitsTableModel ) {
			KMyMoneyTransactionSplitsTableModel tblModel = (KMyMoneyTransactionSplitsTableModel) temp;
			int max = tblModel.getRowCount();
			for ( int i = 0; i < max; i++ ) {
				if ( tblModel.getTransactionSplit(i).getTransaction().getID().equals( trx.getID() ) ) {
					getTransactionTable().getSelectionModel().setSelectionInterval(i, i);
					return;
				}
			}
		}
		
		getSingleTransactionPanel().setTransaction(trx);
		getSingleTransactionPanel().setVisible(true);
		getSelectionSummaryPanel().setVisible(false);
		getSummaryPanel().setPreferredSize(getSingleTransactionPanel().getPreferredSize());
	}

	/**
	 * @param aSplitActions the actions we shall offer on splits.
	 */
	public void setSplitActions(final Collection<TransactionSplitAction> aSplitActions) {
		LOGGER.debug("setSplitActions: TransactionsPanel is given " + (mySplitActions == null ? "no" : mySplitActions.size()) + " split-actions"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
