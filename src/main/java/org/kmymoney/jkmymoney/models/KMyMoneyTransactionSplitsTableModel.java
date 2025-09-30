package org.kmymoney.jkmymoney.models;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Set;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.kmymoney.api.read.KMyMoneyTransactionSplit;
import org.kmymoney.api.write.KMyMoneyWritableTransactionSplit;
import org.kmymoney.viewer.models.KMyMoneySimpleTransactionSplitsTableModel;

/**
 * Version  of KMyMoneySimpleTransactionSplitsTableModel that reacts to
 * updates.
 */
public class KMyMoneyTransactionSplitsTableModel extends KMyMoneySimpleTransactionSplitsTableModel {
	//TODO: allow editing by the user of transaction-descriptions+dates and split-values for transactions with exactly 2 splits.

	/**
	 * Listener that we attach to the account and the displayed splits
	 * to be informed about changes.
	 */
	private final MyPropertyChangeListener myPropertyChangeListener = new MyPropertyChangeListener();

	/**
	 * @param aList splits to display
	 */
	public KMyMoneyTransactionSplitsTableModel(final List<? extends KMyMoneyTransactionSplit> aList) {
		super(aList);

		for (KMyMoneyTransactionSplit split : aList) {
			if (split instanceof KMyMoneyWritableTransactionSplit) {
				KMyMoneyWritableTransactionSplit ws = (KMyMoneyWritableTransactionSplit) split;
				// ::TODO
				// ws.addPropertyChangeListener(myPropertyChangeListener);
			}
		}
	}

	/**
	 * Empty  model.
	 */
	public KMyMoneyTransactionSplitsTableModel() {
		super();
	}

	/**
	 * (c) 2009 by <a href="http://Wolschon.biz>Wolschon Softwaredesign und Beratung</a>.<br/>
	 * Project: jkmymoneyLib-GPL<br/>
	 * MyPropertyChangeListener<br/>
	 * created: 12.10.2009 <br/>
	 *<br/><br/>
	 * Listener that we attach to the account and the displayed splits
	 * to be informed about changes.
	 * @author  <a href="mailto:Marcus@Wolschon.biz">fox</a>
	 */
	private final class MyPropertyChangeListener implements
			PropertyChangeListener {
		/**
		 * {@inheritDoc}
		 */
		public void propertyChange(final PropertyChangeEvent aEvt) {
			Set<TableModelListener> listeners = getTableModelListeners();
			TableModelEvent event = new TableModelEvent(KMyMoneyTransactionSplitsTableModel.this);
			for (TableModelListener tableModelListener : listeners) {
				tableModelListener.tableChanged(event);
			}
		}
	}

}
