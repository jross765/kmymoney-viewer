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
import javax.swing.JToolTip;
import javax.swing.SwingUtilities;

import org.kmymoney.api.read.KMyMoneyTransaction;
import org.kmymoney.api.read.KMyMoneyTransactionSplit;
import org.kmymoney.viewer.Const;
import org.kmymoney.viewer.actions.TransactionSplitAction;
import org.kmymoney.viewer.models.KMyMoneyTransactionSplitsTableModel;
import org.kmymoney.viewer.models.SingleTransactionTableModel;
import org.kmymoney.viewer.widgets.MultiLineToolTip;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This Panel shows all splits of a single transaction.
 */
public class ShowTransactionPanel extends JPanel {

	static final Logger LOGGER = LoggerFactory.getLogger(ShowTransactionPanel.class);

	// ::MAGIC
	private static final int DEFAULT_WIDTH = 200;
	private static final int DEFAULT_HEIGHT = 200;

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
		final KMyMoneyTransactionSplit splt = model.getTransactionSplit(row - 1);
		if ( splt != null ) {
			Collection<TransactionSplitAction> splitActions = getSplitActions();
			for ( TransactionSplitAction spltAct : splitActions ) {
				final TransactionSplitAction spltAct2 = spltAct;
				JMenuItem newMenuItem = new JMenuItem(new Action() {

					@Override
					public void addPropertyChangeListener(final PropertyChangeListener aListener) {
						spltAct2.addPropertyChangeListener(aListener);
					}

					@Override
					public Object getValue(final String aKey) {
						return spltAct2.getValue(aKey);
					}

					@Override
					public boolean isEnabled() {
						spltAct2.setSplit(splt);
						return spltAct2.isEnabled();
					}

					@Override
					public void putValue(final String aKey, final Object aValue) {
						spltAct2.putValue(aKey, aValue);
					}

					@Override
					public void removePropertyChangeListener(final PropertyChangeListener aListener) {
						spltAct2.removePropertyChangeListener(aListener);
					}

					@Override
					public void setEnabled(final boolean aB) {
						spltAct2.setEnabled(aB);
					}

					@Override
					public void actionPerformed(final ActionEvent aE) {
						spltAct2.setSplit(splt);
						spltAct2.actionPerformed(aE);
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
	 * @param trx The transaction to set.
	 */
	public void setTransaction(final KMyMoneyTransaction trx) {
		Object old = myTransaction;
		if (old == trx) {
			return; // nothing has changed
		}
		myTransaction = trx;

		SingleTransactionTableModel model = null;

		if ( trx == null ) {
			model = new SingleTransactionTableModel();
			setPreferredSize(new Dimension(0, 0));
			invalidate();
		} else {
			model = new SingleTransactionTableModel(trx);
			setPreferredSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
			invalidate();
		}
		
		setModel(model);
	}

	/**
	 * The model of our ${@link #trxTab}.
	 */
	private KMyMoneyTransactionSplitsTableModel model;

	/**
	 * The table showing the splits.
	 */
	private JTable trxTab;

	/**
	 * My SCrollPane over {@link #trxTab}.
	 */
	private JScrollPane trxTabScrollPane;


	/**
	 * @return Returns the model.
	 * @see #model
	 */
	public KMyMoneyTransactionSplitsTableModel getModel() {
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
		trxTab.setAutoCreateRowSorter(false);
		
		// ---
		// BEGIN col widths
		FontMetrics metrics = Toolkit.getDefaultToolkit().getFontMetrics(trxTab.getFont());

		trxTab.getColumn(Messages_ShowTransactionPanel.getString("ShowTransactionPanel.4")).setPreferredWidth( //$NON-NLS-1$
				SwingUtilities.computeStringWidth(metrics, SingleTransactionTableModel.DATE_FORMAT.format(LocalDateTime.now())) + Const.TABLE_COL_EXTRA_WIDTH);

		int currencyWidthDefault = SwingUtilities.computeStringWidth(metrics, SingleTransactionTableModel.DEFAULT_CURRENCY_FORMAT.format(Const.TABLE_COL_AMOUNT_WIDTH_VAL_SMALL));
		int currencyWidthMax     = SwingUtilities.computeStringWidth(metrics, SingleTransactionTableModel.DEFAULT_CURRENCY_FORMAT.format(Const.TABLE_COL_AMOUNT_WIDTH_VAL_BIG));

		trxTab.getColumn(Messages_ShowTransactionPanel.getString("ShowTransactionPanel.5")).setPreferredWidth(currencyWidthDefault); //$NON-NLS-1$
		trxTab.getColumn(Messages_ShowTransactionPanel.getString("ShowTransactionPanel.6")).setPreferredWidth(currencyWidthDefault); //$NON-NLS-1$
		trxTab.getColumn(Messages_ShowTransactionPanel.getString("ShowTransactionPanel.7")).setPreferredWidth(SwingUtilities.computeStringWidth(metrics, KMyMoneyTransactionSplit.Action.REMOVE_SHARES.toString())); //$NON-NLS-1$

		trxTab.getColumn(Messages_ShowTransactionPanel.getString("ShowTransactionPanel.4")).setMinWidth(Const.TABLE_COL_MIN_WIDTH); //$NON-NLS-1$
		trxTab.getColumn(Messages_ShowTransactionPanel.getString("ShowTransactionPanel.5")).setMinWidth(Const.TABLE_COL_MIN_WIDTH); //$NON-NLS-1$
		trxTab.getColumn(Messages_ShowTransactionPanel.getString("ShowTransactionPanel.6")).setMinWidth(Const.TABLE_COL_MIN_WIDTH); //$NON-NLS-1$
		trxTab.getColumn(Messages_ShowTransactionPanel.getString("ShowTransactionPanel.7")).setMinWidth(Const.TABLE_COL_MIN_WIDTH); //$NON-NLS-1$

		trxTab.getColumn(Messages_ShowTransactionPanel.getString("ShowTransactionPanel.4")).setMaxWidth(Const.TABLE_COL_MAX_WIDTH); //$NON-NLS-1$
		trxTab.getColumn(Messages_ShowTransactionPanel.getString("ShowTransactionPanel.5")).setMaxWidth(currencyWidthMax); //$NON-NLS-1$
		trxTab.getColumn(Messages_ShowTransactionPanel.getString("ShowTransactionPanel.6")).setMaxWidth(currencyWidthMax); //$NON-NLS-1$
		trxTab.getColumn(Messages_ShowTransactionPanel.getString("ShowTransactionPanel.7")).setMaxWidth((int) ( SwingUtilities.computeStringWidth(metrics, KMyMoneyTransactionSplit.Action.REINVEST_DIVIDEND.toString()) * FACTOR_COL_WIDTH_ACTION ) ); //$NON-NLS-1$
		// END col widths
		// ---
	}

	/**
	 * This method initializes transactionTableScrollPane.
	 *
	 * @return javax.swing.JScrollPane
	 */
	private JScrollPane getTransactionTableScrollPane() {
		if (trxTabScrollPane == null) {
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
		if (trxTab == null) {
			trxTab = new JTable() {
				// *****
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
					if ( rowIndex > 0 ) {
						KMyMoneyTransactionSplitsTableModel model = (KMyMoneyTransactionSplitsTableModel) getModel();
						KMyMoneyTransactionSplit localSplit = model.getTransactionSplit(rowIndex - 1);
						StringBuilder output = new StringBuilder();
						output.append(localSplit.toString());

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
			
			setModel(new SingleTransactionTableModel());
			trxTab.addMouseListener(new MouseAdapter() {

				/** show ShowTransactionPanel#getCellPopupMenu() if mousePressed is a popupTrigger on this platform.
				 * @see java.awt.event.MouseAdapter#mouseReleased(java.awt.event.MouseEvent)
				 */
				@Override
				public void mousePressed(final MouseEvent aE) {
					try {
						if ( aE.isPopupTrigger() ) {
							int row = trxTab.rowAtPoint(aE.getPoint());
							if ( row > 0 ) {
								getCellPopupMenu(row).show((JComponent) aE.getSource(),
										aE.getX(), aE.getY());
							} else {
								LOGGER.debug("getTransactionTable.mousePressed: No split-row below mouse found, not showing popup-menu"); //$NON-NLS-1$
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
						if ( aE.isPopupTrigger() ) {
							int row = trxTab.rowAtPoint(aE.getPoint());
							if ( row > 0 ) {
								getCellPopupMenu(row).show((JComponent) aE.getSource(),
										aE.getX(), aE.getY());
							} else {
								LOGGER.debug("getTransactionTable.mouseReleased: No split-row below mouse found, not showing popup-menu"); //$NON-NLS-1$
							}
						}
					} catch (Exception e) {
						LOGGER.error("getTransactionTable.mouseReleased: Error showing popup-menu", e); //$NON-NLS-1$
					}
				}
			});
		}
		
		return trxTab;
	}


	/**
	 * Used to populate context-menus.
	 *
	 * @param aSplitActions the actions we are to support on splits
	 */
	public void setSplitActions(final Collection<TransactionSplitAction> aSplitActions) {
		mySplitActions = aSplitActions;
		LOGGER.debug("setSplitActions: ShowTransactionPanel is given " + (mySplitActions == null ? "no" : mySplitActions.size()) + " split-actions"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	/**
	 * @return the splitActions
	 */
	protected Collection<TransactionSplitAction> getSplitActions() {
		LOGGER.debug("getSplitActions: howTransactionPanel has " + (mySplitActions == null ? "no" : mySplitActions.size()) + " split-actions"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		return mySplitActions;
	}
}
