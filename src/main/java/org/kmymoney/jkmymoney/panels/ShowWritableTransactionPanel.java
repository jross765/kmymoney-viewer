package org.kmymoney.jkmymoney.panels;

import java.awt.Dimension;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;
import java.util.logging.Logger;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;

import org.kmymoney.api.read.KMyMoneyAccount;
import org.kmymoney.api.read.KMyMoneyTransaction;
import org.kmymoney.viewer.panels.ShowTransactionPanel;
import org.kmymoney.viewer.panels.SingleTransactionTableModel;


/**
 * This is a variant of {@link org.kmymoney.viewer.panels.ShowTransactionPanel} that also allows
 * to edit the transaction.
 */
public class ShowWritableTransactionPanel extends ShowTransactionPanel {

	/**
	 * Automatically created logger for debug and error-output.
	 */
	static final Logger LOG = Logger
			.getLogger(ShowWritableTransactionPanel.class.getName());

	//------------------------ support for propertyChangeListeners ------------------

	/**
	 * support for firing PropertyChangeEvents.
	 * (gets initialized only if we really have listeners)
	 */
	private volatile PropertyChangeSupport myPropertyChange = null;

	/**
	 * Returned value may be null if we never had listeners.
	 * @return Our support for firing PropertyChangeEvents
	 */
	protected PropertyChangeSupport getPropertyChangeSupport() {
		return myPropertyChange;
	}

	/**
	 * Add a PropertyChangeListener to the listener list.
	 * The listener is registered for all properties.
	 *
	 * @param listener  The PropertyChangeListener to be added
	 */
	@Override
	public final void addPropertyChangeListener(
			final PropertyChangeListener listener) {
		if (myPropertyChange == null) {
			myPropertyChange = new PropertyChangeSupport(this);
		}
		myPropertyChange.addPropertyChangeListener(listener);
	}

	/**
	 * Add a PropertyChangeListener for a specific property.  The listener
	 * will be invoked only when a call on firePropertyChange names that
	 * specific property.
	 *
	 * @param propertyName  The name of the property to listen on.
	 * @param listener  The PropertyChangeListener to be added
	 */
	@Override
	public final void addPropertyChangeListener(
			final String propertyName,
			final PropertyChangeListener listener) {
		if (myPropertyChange == null) {
			myPropertyChange = new PropertyChangeSupport(this);
		}
		myPropertyChange.addPropertyChangeListener(propertyName, listener);
	}

	/**
	 * Remove a PropertyChangeListener for a specific property.
	 *
	 * @param propertyName  The name of the property that was listened on.
	 * @param listener  The PropertyChangeListener to be removed
	 */
	@Override
	public final void removePropertyChangeListener(
			final String propertyName,
			final PropertyChangeListener listener) {
		if (myPropertyChange != null) {
			myPropertyChange.removePropertyChangeListener(propertyName,
					listener);
		}
	}

	/**
	 * Remove a PropertyChangeListener from the listener list.
	 * This removes a PropertyChangeListener that was registered
	 * for all properties.
	 *
	 * @param listener  The PropertyChangeListener to be removed
	 */
	@Override
	public synchronized void removePropertyChangeListener(
			final PropertyChangeListener listener) {
		if (myPropertyChange != null) {
			myPropertyChange.removePropertyChangeListener(listener);
		}
	}

	//-------------------------------------------------------

	/**
	 * Just an overridden ToString to return this classe's name
	 * and hashCode.
	 * @return className and hashCode
	 */
	@Override
	public String toString() {
		return "ShowWritableTransactionPanel@" + hashCode();
	}

	/**
	 * @param aTransaction The transaction to set. MAY BE NULL
	 * @see #myTransaction
	 */
	@Override
	public void setTransaction(final KMyMoneyTransaction aTransaction) {

		Object old = getTransaction();
		if (old == aTransaction) {
			return; // nothing has changed
		}
		super.setTransaction(aTransaction);

		SingleTransactionTableModel model = null;

		if (aTransaction == null) {
			model = new SingleWritableTransactionTableModel();
			setPreferredSize(new Dimension(0, 0));
			invalidate();
			super.setModel(model);
		} else {
			model = new SingleWritableTransactionTableModel(aTransaction);
			setPreferredSize(new Dimension(200, 200));
			invalidate();
			setModel(model);
		}
	}

	/**
	 * This method initializes transactionTable.
	 *
	 * @return javax.swing.JTable
	 */
	@Override
	protected JTable getTransactionTable() {
		JTable transactionTable = super.getTransactionTable();
		if (!(transactionTable.getModel() instanceof SingleWritableTransactionTableModel)) {
			transactionTable.setModel(new SingleWritableTransactionTableModel());
		}
		return transactionTable;
	}

	/**
	 * @param aModel The model to set.
	 * @see #model
	 */
	@Override
	protected void setModel(final SingleTransactionTableModel aModel) {
		super.setModel(aModel);

		// if editing is possible, install a jcomboBox as an editor for the accounts
		if (aModel != null && aModel instanceof SingleWritableTransactionTableModel) {
			KMyMoneyTransaction transaction = aModel.getTransaction();
			if (transaction == null) {
				throw new IllegalArgumentException("Given model has no transaction");
			}
			JComboBox accountsCombo = new JComboBox() {

				/**
				 * ${@inheritDoc}.
				 */
				@Override
				public String getToolTipText() {
					Object selectedItem = getSelectedItem();
					if (selectedItem != null) {
						return selectedItem.toString();
					}
					return super.getToolTipText();
				}
			};
			accountsCombo.setToolTipText("Account-name"); //make sure a tooltip-manager exists
			if (transaction != null) {
				Collection<? extends KMyMoneyAccount> accounts = transaction.getKMyMoneyFile().getAccounts();
				for (KMyMoneyAccount kmymoneyAccount : accounts) {
					accountsCombo.addItem(kmymoneyAccount.getQualifiedName());
				}
			}

//            getTransactionTable().getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(new JTextField()));
//            getTransactionTable().getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(new JTextField()));
//            getTransactionTable().getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(new JTextField()));
//            getTransactionTable().getColumnModel().getColumn(3).setCellEditor(new DefaultCellEditor(accountsCombo));
//            getTransactionTable().getColumnModel().getColumn(4).setCellEditor(new DefaultCellEditor(new JTextField()));
//            getTransactionTable().getColumnModel().getColumn(5).setCellEditor(new DefaultCellEditor(new JTextField()));
			getTransactionTable().getColumn("account").setCellEditor(new DefaultCellEditor(accountsCombo));
		}
	}
}
