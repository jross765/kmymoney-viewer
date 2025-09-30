package org.kmymoney.jkmymoney.panels;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;

import org.java.plugin.PluginManager;
import org.java.plugin.registry.Extension;
import org.java.plugin.registry.ExtensionPoint;
import org.java.plugin.registry.PluginDescriptor;
import org.kmymoney.api.read.KMyMoneyAccount;
import org.kmymoney.api.read.KMyMoneyTransactionSplit;

//other imports


import org.kmymoney.api.write.KMyMoneyWritableTransactionSplit;
import org.kmymoney.jkmymoney.models.KMyMoneyAccountTransactionsTableModel;
import org.kmymoney.jkmymoney.models.KMyMoneyTransactionSplitsTableModel;
import org.kmymoney.jkmymoney.plugin.TransactionMenuAction;
import org.kmymoney.viewer.models.KMyMoneySimpleAccountTransactionsTableModel;
import org.kmymoney.viewer.panels.ShowTransactionPanel;
import org.kmymoney.viewer.panels.TransactionsPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Variant of TransactionsPanel that allows editing the transactions.
 */
public class WritableTransactionsPanel extends TransactionsPanel {
	/**
	 * Our logger for debug- and error-output.
	 */
	static final Logger LOGGER = LoggerFactory.getLogger(WritableTransactionsPanel.class);

	/**
	 * (c) 2008 by <a href="http://Wolschon.biz>Wolschon Softwaredesign und Beratung</a>.<br/>
	 * Project: jkmymoneyLib-GPL<br/>
	 * TransactionMenuActionMenuAction<br/>
	 * created: 12.11.2008 <br/>
	 * <br/><br/>
	 * <b>Have a plugin react to it's menu-point in the context-menu.</b>
	 *
	 * @author <a href="mailto:Marcus@Wolschon.biz">fox</a>
	 */
	public class TransactionMenuActionMenuAction implements ActionListener {
		/**
		 * The plugin.
		 */
		private final Extension ext;

		/**
		 * The name of the plugin.
		 */
		private final String pluginName;


		/**
		 * @param aExt        the plugin to execute
		 * @param aPluginName the name to display
		 */
		public TransactionMenuActionMenuAction(final Extension aExt, final String aPluginName) {
			ext = aExt;
			pluginName = aPluginName;
		}

		/**
		 * {@inheritDoc}
		 *
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(final ActionEvent aE) {
			try {
				JTable table = WritableTransactionsPanel.this.getTransactionTable();
				KMyMoneySimpleAccountTransactionsTableModel model = (KMyMoneySimpleAccountTransactionsTableModel) table.getModel();
				int[] selectedRows = table.getSelectedRows();
				Collection<KMyMoneyWritableTransactionSplit> transactions = new ArrayList<KMyMoneyWritableTransactionSplit>(selectedRows.length);
				for (int i : selectedRows) {
					transactions.add((KMyMoneyWritableTransactionSplit) model.getTransactionSplit(i));
				}


				// Activate plug-in that declares extension.
				getPluginManager().activatePlugin(ext.getDeclaringPluginDescriptor().getId());
				// Get plug-in class loader.
				ClassLoader classLoader = getPluginManager().getPluginClassLoader(
						ext.getDeclaringPluginDescriptor());
				// Load Tool class.
				Class toolCls = classLoader.loadClass(
						ext.getParameter("class").valueAsString());
				// Create Tool instance.
				Object o = toolCls.newInstance();
				if (!(o instanceof TransactionMenuAction)) {
					LOG.error("Plugin '" + pluginName + "' does not implement TransactionMenuAction-interface.");
					JOptionPane.showMessageDialog(WritableTransactionsPanel.this, "Error",
							"Plugin '" + pluginName + "' does not implement v-interface.",
							JOptionPane.ERROR_MESSAGE);
					return;

				}
				TransactionMenuAction action = (TransactionMenuAction) o;
				try {
					WritableTransactionsPanel.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					action.handleSelectedTransactions(transactions);
				} catch (Exception e1) {
					LOG.error("MenuAction via Plugin '" + pluginName + "' failed.", e1);
					JOptionPane.showMessageDialog(WritableTransactionsPanel.this, "Error",
							"MenuAction via Plugin '" + pluginName + "' failed.\n"
									+ "[" + e1.getClass().getName() + "]: " + e1.getMessage(),
							JOptionPane.ERROR_MESSAGE);
				} finally {
					WritableTransactionsPanel.this.setCursor(Cursor.getDefaultCursor());
				}
			} catch (Exception e1) {
				LOGGER.error("Could not activate requested import-plugin '" + pluginName + "'.", e1);
				JOptionPane.showMessageDialog(WritableTransactionsPanel.this, "Error",
						"Could not activate requested MenuAction-plugin '" + pluginName + "'.\n"
								+ "[" + e1.getClass().getName() + "]: " + e1.getMessage(),
						JOptionPane.ERROR_MESSAGE);
			}

		}

	}

	/**
	 * the context-menu for the transactionTable.
	 */
	private JPopupMenu myContextMenu;

	/**
	 * The main-entry-point to our plugin-api.
	 * May be null to disable plugins.
	 */
	private PluginManager pluginManager = null;

	/**
	 * The descriptor for our top-level application-plugin.
	 * May be null to disable plugins.
	 */
	private PluginDescriptor pluginDescriptor = null;

	/**
	 *
	 */
	public WritableTransactionsPanel() {
		super();
	}

	/**
	 * @param aPluginDescriptor for extensing the context-menu with plugins.
	 * @param aPluginManager    for extensing the context-menu with plugins.
	 */
	public WritableTransactionsPanel(final PluginManager aPluginManager, final PluginDescriptor aPluginDescriptor) {
		super();
		setPluginManager(aPluginManager);
		setPluginDescriptor(aPluginDescriptor);
	}

	/**
	 * @param account if null, an empty table will be shown.
	 */
	public WritableTransactionsPanel(final KMyMoneyAccount account) {
		super();
		setAccount(account);
	}


	/**
	 * Automatically created logger for debug and error-output.
	 */
	private static final Logger LOG = LoggerFactory
			.getLogger(WritableTransactionsPanel.class.getName());


	/**
	 * The panel to show a single transaction.
	 */
	private ShowWritableTransactionPanel mySingleWritableTransactionPanel;

	/**
	 * Just an overridden ToString to return this classe's name
	 * and hashCode.
	 *
	 * @return className and hashCode
	 */
	@Override
	public String toString() {
		return "WritableTransactionsPanel@" + hashCode();
	}


	/**
	 * Give an account who's transactions to display.
	 *
	 * @param account if null, an empty table will be shown.
	 */
	@Override
	public void setAccount(final KMyMoneyAccount account) {

		if (account == null) {
			setModel(new KMyMoneyAccountTransactionsTableModel());
		} else {
			setModel(new KMyMoneyAccountTransactionsTableModel(account));
		}
	}

	/**
	 * Instead of displaying the transactions of an account,
	 * display the given (ordered) list of transactions.
	 *
	 * @param aTransactionList
	 */
	public void setDisplayedTransactions(final List<KMyMoneyTransactionSplit> aTransactionList) {
		setModel(new KMyMoneyTransactionSplitsTableModel(aTransactionList));
	}

	/**
	 * This method initializes ShowTransactionPanel.
	 *
	 * @return javax.swing.JPanel
	 */

	@Override
	protected ShowTransactionPanel getSingleTransactionPanel() {
		return getSingleWritableTransactionPanel();
	}

	/**
	 * This method initializes ShowTransactionPanel.
	 *
	 * @return javax.swing.JPanel
	 */
	protected ShowTransactionPanel getSingleWritableTransactionPanel() {
		if (mySingleWritableTransactionPanel == null) {
			mySingleWritableTransactionPanel = new ShowWritableTransactionPanel();
			mySingleWritableTransactionPanel.setSplitActions(getSplitActions());
		}
		return mySingleWritableTransactionPanel;
	}
//
//    //------------------------ support for propertyChangeListeners ------------------
//
//    /**
//     * support for firing PropertyChangeEvents.
//     * (gets initialized only if we really have listeners)
//     */
//    private volatile PropertyChangeSupport myPropertyChange = null;
//
//    /**
//     * Returned value may be null if we never had listeners.
//     * @return Our support for firing PropertyChangeEvents
//     */
//    protected PropertyChangeSupport getPropertyChangeSupport() {
//        return myPropertyChange;
//    }
//
//    /**
//     * Add a PropertyChangeListener to the listener list.
//     * The listener is registered for all properties.
//     *
//     * @param listener  The PropertyChangeListener to be added
//     */
//    public final void addPropertyChangeListener(
//                                                final PropertyChangeListener listener) {
//        if (myPropertyChange == null) {
//            myPropertyChange = new PropertyChangeSupport(this);
//        }
//        myPropertyChange.addPropertyChangeListener(listener);
//    }
//
//    /**
//     * Add a PropertyChangeListener for a specific property.  The listener
//     * will be invoked only when a call on firePropertyChange names that
//     * specific property.
//     *
//     * @param propertyName  The name of the property to listen on.
//     * @param listener  The PropertyChangeListener to be added
//     */
//    public final void addPropertyChangeListener(
//                                                final String propertyName,
//                                                final PropertyChangeListener listener) {
//        if (myPropertyChange == null) {
//            myPropertyChange = new PropertyChangeSupport(this);
//        }
//        myPropertyChange.addPropertyChangeListener(propertyName, listener);
//    }
//
//    /**
//     * Remove a PropertyChangeListener for a specific property.
//     *
//     * @param propertyName  The name of the property that was listened on.
//     * @param listener  The PropertyChangeListener to be removed
//     */
//    public final void removePropertyChangeListener(
//                                                   final String propertyName,
//                                                   final PropertyChangeListener listener) {
//        if (myPropertyChange != null) {
//            myPropertyChange.removePropertyChangeListener(propertyName,
//                    listener);
//        }
//    }
//
//    /**
//     * Remove a PropertyChangeListener from the listener list.
//     * This removes a PropertyChangeListener that was registered
//     * for all properties.
//     *
//     * @param listener  The PropertyChangeListener to be removed
//     */
//    public synchronized void removePropertyChangeListener(
//                                                          final PropertyChangeListener listener) {
//        if (myPropertyChange != null) {
//            myPropertyChange.removePropertyChangeListener(listener);
//        }
//    }
//
//    //-------------------------------------------------------

	/**
	 * {@inheritDoc}
	 *
	 * @see org.kmymoney.jkmymoney.panels.TransactionsPanel#getTransactionTable()
	 */
	@Override
	protected JTable getTransactionTable() {
		JTable transactionTable = super.getTransactionTable();
		transactionTable.add(getTransactionTableContextMenu());
		return transactionTable;
	}

	/**
	 * @return the context-menu for the transactionTable.
	 * @see #getTransactionTable()
	 */
	private Component getTransactionTableContextMenu() {
		if (myContextMenu == null) {
			myContextMenu = new JPopupMenu();

			PluginManager manager = getPluginManager();
			// if we are configured for the plugin-api
			if (manager != null) {
				ExtensionPoint toolExtPoint = manager.getRegistry().getExtensionPoint(
						getPluginDescriptor().getId(), "TransactionMenuAction");
				for (Iterator<Extension> it = toolExtPoint.getConnectedExtensions().iterator(); it.hasNext(); ) {
					Extension ext = it.next();
					String pluginName = "unknown";

					try {
						pluginName = ext.getParameter("name").valueAsString();
						JMenuItem newMenuItem = new JMenuItem();
						newMenuItem.putClientProperty("extension", ext);
						newMenuItem.setText(pluginName);
						newMenuItem.addActionListener(new TransactionMenuActionMenuAction(ext, pluginName));
						myContextMenu.add(newMenuItem);
					} catch (Exception e) {
						LOG.error("cannot load TransactionMenuAction-Plugin '" + pluginName + "'", e);
						JOptionPane.showMessageDialog(this, "Error",
								"Cannot load TransactionMenuAction-Plugin '" + pluginName + "'",
								JOptionPane.ERROR_MESSAGE);
					}
				}
			}

		}
		return myContextMenu;
	}

	/**
	 * The main-entry-point to our plugin-api.
	 * (may be null to disable plugins.)
	 *
	 * @return the pluginManager
	 */
	public PluginManager getPluginManager() {
		return pluginManager;
	}

	/**
	 * The main-entry-point to our plugin-api.
	 * (may be null to disable plugins.)
	 *
	 * @param aPluginManager the pluginManager to set
	 */
	public void setPluginManager(final PluginManager aPluginManager) {
		pluginManager = aPluginManager;
	}

	/**
	 * The descriptor for our top-level application-plugin.
	 * (may be null to disable plugins.)
	 *
	 * @return the pluginDescriptor
	 */
	public PluginDescriptor getPluginDescriptor() {
		return pluginDescriptor;
	}

	/**
	 * The descriptor for our top-level application-plugin.
	 * (may be null to disable plugins.)
	 *
	 * @param aPluginDescriptor the pluginDescriptor to set
	 */
	public void setPluginDescriptor(final PluginDescriptor aPluginDescriptor) {
		pluginDescriptor = aPluginDescriptor;
	}

}
