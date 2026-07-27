package org.kmymoney.viewer.panels;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import org.kmymoney.api.read.KMyMoneyTransactionSplit;
import org.kmymoney.viewer.models.KMyMoneyTransactionSplitsTableModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Renderer that turns any value that contains the string "TODO" bold.
 */
public class DesriptionCellRenderer implements TableCellRenderer {

	private static final Logger LOGGER = LoggerFactory.getLogger(DesriptionCellRenderer.class.getName());

	// ---------------------------------------------------------------
	// ::MAGIC

	private static final String TAG_PREFIX    = "::";
	
	private static final String TODO_TAG_RAW  = "TODO";
	private static final String CHECK_TAG_RAW = "CHECK";
	private static final String OK_TAG_RAW    = "OK";

	private static final String TODO_TAG  = TAG_PREFIX + TODO_TAG_RAW;
	private static final String CHECK_TAG = TAG_PREFIX + CHECK_TAG_RAW;
	private static final String OK_TAG    = TAG_PREFIX + OK_TAG_RAW;

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
	public final void addPropertyChangeListener(
			final PropertyChangeListener listener) {
		if ( myPropertyChange == null ) {
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
	public final void addPropertyChangeListener(final String propertyName,
			final PropertyChangeListener listener) {
		if ( myPropertyChange == null ) {
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
	public final void removePropertyChangeListener(final String propertyName,
			final PropertyChangeListener listener) {
		if ( myPropertyChange != null ) {
			myPropertyChange.removePropertyChangeListener(propertyName, listener);
		}
	}

	/**
	 * Remove a PropertyChangeListener from the listener list.
	 * This removes a PropertyChangeListener that was registered
	 * for all properties.
	 *
	 * @param listener  The PropertyChangeListener to be removed
	 */
	public synchronized void removePropertyChangeListener(
			final PropertyChangeListener listener) {
		if ( myPropertyChange != null ) {
			myPropertyChange.removePropertyChangeListener(listener);
		}
	}

	//-------------------------------------------------------

	/** 
	 * ${@inheritDoc}.
	 */
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {

		JLabel renderer = new JLabel(value == null ? "" : value.toString());
		Font f = renderer.getFont();
		// renderer.setFont(f.deriveFont(f.getStyle() ^ Font.BOLD));
		renderer.setFont(f.deriveFont(f.getStyle()));

		try {
			markTokens(renderer, renderer.getText());

			// mark unbalanced transactions in red.
			TableModel model = table.getModel();
			if ( model instanceof KMyMoneyTransactionSplitsTableModel ) {
				KMyMoneyTransactionSplitsTableModel tmodel = (KMyMoneyTransactionSplitsTableModel) model;
				KMyMoneyTransactionSplit split = tmodel.getTransactionSplit(row);
				if ( split != null ) {
					if ( split.getTransaction() != null ) {
						try {
							List<? extends KMyMoneyTransactionSplit> spltList = split.getTransaction().getSplits();
							for ( KMyMoneyTransactionSplit splt : spltList ) {
								if ( splt != null ) {
									markTokens(renderer, splt.getMemo());
								}
							}
						} catch (Exception e) {
							LOGGER.error("getTableCellRendererComponent: While traversing splits", e);
						}
					}
					markUnbalanced(renderer, split);
				}
			}
		} catch (Exception e) {
			LOGGER.error("getTableCellRendererComponent: ", e);
		}

		// ideas for future enhancements: allow plugins to display icons.
		// e.g. display a hbci-icon if this is an hbci-imported transaction
		// to display the original transaction-details on click.
		return renderer;
	}

	//-------------------------------------------------------

	/**
	 * Check whether transaction is balanced and mark it in pink, if not.
	 *  
	 * @param renderer the renderer to modify it's style
	 * @param split the transaction we display
	 */
	private void markUnbalanced(final JLabel renderer, final KMyMoneyTransactionSplit split) {
		try {
			if ( split.getTransaction() != null ) {
				if ( ! split.getTransaction().isBalanced() ) {
					Font f = renderer.getFont();
					renderer.setFont(f.deriveFont(f.getStyle() | Font.BOLD));
					renderer.setForeground(Color.RED);
				}
			} else {
				renderer.setForeground(Color.GRAY);
			}
		} catch (Exception e) {
			LOGGER.error("markUnbalanced: ", e);
			renderer.setForeground(Color.GRAY);
		}
	}

	/**
	 * Change text format depending on whether specific tags are contained in it.<br/>

	 * @param renderer the renderer to modify its style
	 */
	private void markTokens(final JLabel renderer, final String text) {
		try {
			if ( text.contains(TODO_TAG) ) {
				Font f = renderer.getFont();
				renderer.setFont(f.deriveFont(f.getStyle() | Font.BOLD));
				renderer.setForeground(Color.BLUE);
			} else if ( text.contains(CHECK_TAG) ) {
				Font f = renderer.getFont();
				renderer.setFont(f.deriveFont(f.getStyle() | Font.BOLD));
				renderer.setForeground(Color.ORANGE.darker());
			} else if ( text.contains(OK_TAG) ) {
				Font f = renderer.getFont();
				renderer.setFont(f.deriveFont(f.getStyle() | Font.BOLD));
				renderer.setForeground(Color.GREEN.darker());
			}
		} catch (Exception e) {
			LOGGER.error("markTokens: ", e);
//			renderer.setForeground(Color.GRAY);
		}
	}

	//-------------------------------------------------------

	public String toString() {
		return "DesriptionCellRenderer@" + hashCode();
	}

}

