package org.kmymoney.viewer.panels;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.time.LocalDateTime;
import java.util.Collection;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

import org.kmymoney.api.read.KMyMoneyTransaction;
import org.kmymoney.api.read.KMyMoneyTransactionSplit;
import org.kmymoney.viewer.Const;
import org.kmymoney.viewer.actions.TransactionSplitAction;
import org.kmymoney.viewer.models.KMyMoneyTransactionsSplitsTableModel;
import org.kmymoney.viewer.models.SingleTransactionTableModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This Panel shows all splits of a single transaction.
 */
public class ShowTransactionPanel extends JPanel {

	static final Logger LOGGER = LoggerFactory.getLogger(ShowTransactionPanel.class);

	private static final double FACTOR_COL_WIDTH_ACTION = 1.5;

	/**
	 * for serializing.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The transaction we are showing.
	 */
	private KMyMoneyTransaction myTransaction = null;


	/**
	 * The actions we have on Splits.
	 */
	private Collection<TransactionSplitAction> mySplitActions;


	/**
	 * @param aTransaction The transaction we are showing.
	 */
	public ShowTransactionPanel(final KMyMoneyTransaction aTransaction) {
		super();
		myTransaction = aTransaction;

		initialize();
	}

	/**
	 * initialize the Gui.
	 */
	private void initialize() {
		this.setLayout(new BorderLayout());
		this.add(getTransactionTableScrollPane(), BorderLayout.CENTER);
		setTransaction(getTransaction());
	}

	/**
	 * @param row the row to create a popup for
	 * @return the popup-menu
	 */
	protected JPopupMenu getCellPopupMenu(final int row) {
		JPopupMenu menu = new JPopupMenu();
		final KMyMoneyTransactionSplit split = model.getTransactionSplit(row - 1);
		if (split != null) {
			Collection<TransactionSplitAction> splitActions = getSplitActions();
			for (TransactionSplitAction splitAction2 : splitActions) {
				final TransactionSplitAction splitAction = splitAction2;
				JMenuItem newMenuItem = new JMenuItem(new Action() {

					@Override
					public void addPropertyChangeListener(final PropertyChangeListener aListener) {
						splitAction.addPropertyChangeListener(aListener);
					}

					@Override
					public Object getValue(final String aKey) {
						return splitAction.getValue(aKey);
					}

					@Override
					public boolean isEnabled() {
						splitAction.setSplit(split);
						return splitAction.isEnabled();
					}

					@Override
					public void putValue(final String aKey, final Object aValue) {
						splitAction.putValue(aKey, aValue);
					}

					@Override
					public void removePropertyChangeListener(final PropertyChangeListener aListener) {
						splitAction.removePropertyChangeListener(aListener);
					}

					@Override
					public void setEnabled(final boolean aB) {
						splitAction.setEnabled(aB);
					}

					@Override
					public void actionPerformed(final ActionEvent aE) {
						splitAction.setSplit(split);
						splitAction.actionPerformed(aE);
					}

				});
				menu.add(newMenuItem);
			}

			LOGGER.info("getCellPopupMenu: Showing popup menu with " + splitActions.size() + " split-actions"); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			LOGGER.info("getCellPopupMenu: No split found, not showing popup menu"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return menu;
	}

	/**
	 * make us visible.
	 */
	public ShowTransactionPanel() {
		super();
		myTransaction = null;

		initialize();
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

		Object old = myTransaction;
		if (old == aTransaction) {
			return; // nothing has changed
		}
		myTransaction = aTransaction;

		SingleTransactionTableModel model = null;

		if (aTransaction == null) {
			model = new SingleTransactionTableModel();
			setPreferredSize(new Dimension(0, 0));
			invalidate();
		} else {
			model = new SingleTransactionTableModel(aTransaction);
			setPreferredSize(new Dimension(200, 200));
			invalidate();
		}
		setModel(model);
	}

	/**
	 * The model of our ${@link #transactionTable}.
	 */
	private KMyMoneyTransactionsSplitsTableModel model;

	/**
	 * The table showing the splits.
	 */
	private JTable transactionTable;

	/**
	 * My SCrollPane over {@link #transactionTable}.
	 */
	private JScrollPane transactionTableScrollPane;


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
	protected void setModel(final SingleTransactionTableModel aModel) {
		if (aModel == null) {
			throw new IllegalArgumentException("argument <aModel> is null"); //$NON-NLS-1$
		}

		Object old = model;
		if (old == aModel) {
			return; // nothing has changed
		}
		model = aModel;

		getTransactionTable().setModel(model);
		transactionTable.setAutoCreateRowSorter(false);
		
		// ---
		// BEGIN col widths
		FontMetrics metrics = Toolkit.getDefaultToolkit().getFontMetrics(transactionTable.getFont());

		transactionTable.getColumn(Messages_ShowTransactionPanel.getString("ShowTransactionPanel.4")).setPreferredWidth( //$NON-NLS-1$
				SwingUtilities.computeStringWidth(metrics, SingleTransactionTableModel.DATE_FORMAT.format(LocalDateTime.now())) + Const.TABLE_COL_EXTRA_WIDTH);

		int currencyWidthDefault = SwingUtilities.computeStringWidth(metrics, SingleTransactionTableModel.DEFAULT_CURRENCY_FORMAT.format(Const.TABLE_COL_AMOUNT_WIDTH_VAL_SMALL));
		int currencyWidthMax     = SwingUtilities.computeStringWidth(metrics, SingleTransactionTableModel.DEFAULT_CURRENCY_FORMAT.format(Const.TABLE_COL_AMOUNT_WIDTH_VAL_BIG));

		transactionTable.getColumn(Messages_ShowTransactionPanel.getString("ShowTransactionPanel.5")).setPreferredWidth(currencyWidthDefault); //$NON-NLS-1$
		transactionTable.getColumn(Messages_ShowTransactionPanel.getString("ShowTransactionPanel.6")).setPreferredWidth(currencyWidthDefault); //$NON-NLS-1$
		transactionTable.getColumn(Messages_ShowTransactionPanel.getString("ShowTransactionPanel.7")).setPreferredWidth(SwingUtilities.computeStringWidth(metrics, KMyMoneyTransactionSplit.Action.REMOVE_SHARES.toString())); //$NON-NLS-1$

		transactionTable.getColumn(Messages_ShowTransactionPanel.getString("ShowTransactionPanel.4")).setMinWidth(Const.TABLE_COL_MIN_WIDTH); //$NON-NLS-1$
		transactionTable.getColumn(Messages_ShowTransactionPanel.getString("ShowTransactionPanel.5")).setMinWidth(Const.TABLE_COL_MIN_WIDTH); //$NON-NLS-1$
		transactionTable.getColumn(Messages_ShowTransactionPanel.getString("ShowTransactionPanel.6")).setMinWidth(Const.TABLE_COL_MIN_WIDTH); //$NON-NLS-1$
		transactionTable.getColumn(Messages_ShowTransactionPanel.getString("ShowTransactionPanel.7")).setMinWidth(Const.TABLE_COL_MIN_WIDTH); //$NON-NLS-1$

		transactionTable.getColumn(Messages_ShowTransactionPanel.getString("ShowTransactionPanel.4")).setMaxWidth(Const.TABLE_COL_MAX_WIDTH); //$NON-NLS-1$
		transactionTable.getColumn(Messages_ShowTransactionPanel.getString("ShowTransactionPanel.5")).setMaxWidth(currencyWidthMax); //$NON-NLS-1$
		transactionTable.getColumn(Messages_ShowTransactionPanel.getString("ShowTransactionPanel.6")).setMaxWidth(currencyWidthMax); //$NON-NLS-1$
		transactionTable.getColumn(Messages_ShowTransactionPanel.getString("ShowTransactionPanel.7")).setMaxWidth((int) ( SwingUtilities.computeStringWidth(metrics, KMyMoneyTransactionSplit.Action.REINVEST_DIVIDEND.toString()) * FACTOR_COL_WIDTH_ACTION ) ); //$NON-NLS-1$
		// END col widths
		// ---
	}

	/**
	 * This method initializes transactionTableScrollPane.
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
			transactionTable = new JTable();
			setModel(new SingleTransactionTableModel());
			transactionTable.addMouseListener(new MouseAdapter() {

				/** show ShowTransactionPanel#getCellPopupMenu() if mousePressed is a popupTrigger on this platform.
				 * @see java.awt.event.MouseAdapter#mouseReleased(java.awt.event.MouseEvent)
				 */
				@Override
				public void mousePressed(final MouseEvent aE) {
					try {
						if (aE.isPopupTrigger()) {
							int row = transactionTable.rowAtPoint(aE.getPoint());
							if (row > 0) {
								getCellPopupMenu(row).show((JComponent) aE.getSource(),
										aE.getX(), aE.getY());
							} else {
								LOGGER.info("getTransactionTable.mousePressed: No split-row below mouse found, not showing popup-menu"); //$NON-NLS-1$
							}
						}
					} catch (Exception e) {
						LOGGER.error("getTransactionTable.mousePressed: Error showing popup menu", e); //$NON-NLS-1$
					}
				}

				/** show ShowTransactionPanel#getCellPopupMenu() if mouseReleased is a popupTrigger on this platform.
				 * @see java.awt.event.MouseAdapter#mouseReleased(java.awt.event.MouseEvent)
				 */
				@Override
				public void mouseReleased(final MouseEvent aE) {
					try {
						if (aE.isPopupTrigger()) {
							int row = transactionTable.rowAtPoint(aE.getPoint());
							if (row > 0) {
								getCellPopupMenu(row).show((JComponent) aE.getSource(),
										aE.getX(), aE.getY());
							} else {
								LOGGER.info("getTransactionTable.mouseReleased: No split-row below mouse found, not showing popup-menu"); //$NON-NLS-1$
							}
						}
					} catch (Exception e) {
						LOGGER.error("getTransactionTable.mouseReleased: Error showing popup-menu", e); //$NON-NLS-1$
					}
				}
			});
		}
		return transactionTable;
	}


	/**
	 * Used to populate context-menus.
	 *
	 * @param aSplitActions the actions we are to support on splits
	 */
	public void setSplitActions(final Collection<TransactionSplitAction> aSplitActions) {
		mySplitActions = aSplitActions;
		LOGGER.info("setSplitActions: ShowTransactionPanel is given " + (mySplitActions == null ? "no" : mySplitActions.size()) + " split-actions"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	/**
	 * @return the splitActions
	 */
	protected Collection<TransactionSplitAction> getSplitActions() {
		LOGGER.info("getSplitActions: howTransactionPanel has " + (mySplitActions == null ? "no" : mySplitActions.size()) + " split-actions"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		return mySplitActions;
	}
}
