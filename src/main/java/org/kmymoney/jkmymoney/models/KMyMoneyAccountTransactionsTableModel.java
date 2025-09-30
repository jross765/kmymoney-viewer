/**
 * KMyMoneyAccountTransactionsTableModel.javaTransactionMenuAction.java
 * created: 12.10.2009
 * (c) 2008 by <a href="http://Wolschon.biz">Wolschon Softwaredesign und Beratung</a>
 * This file is part of jkmymoneyLib-GPL by Marcus Wolschon <a href="mailto:Marcus@Wolscon.biz">Marcus@Wolscon.biz</a>.
 * You can purchase support for a sensible hourly rate or
 * a commercial license of this file (unless modified by others) by contacting him directly.
 * <p>
 * jkmymoneyLib-GPL is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * jkmymoneyLib-GPL is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with jkmymoneyLib-V1.  If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * **********************************
 * Editing this file:
 * -For consistent code-quality this file should be checked with the
 * checkstyle-ruleset enclosed in this project.
 * -After the design of this file has settled it should get it's own
 * JUnit-Test that shall be executed regularly. It is best to write
 * the test-case BEFORE writing this class and to run it on every build
 * as a regression-test.
 */

package org.kmymoney.jkmymoney.models;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.kmymoney.api.read.KMyMoneyAccount;
import org.kmymoney.api.read.KMyMoneyTransactionSplit;
import org.kmymoney.api.write.KMyMoneyWritableAccount;
import org.kmymoney.api.write.KMyMoneyWritableTransactionSplit;
import org.kmymoney.viewer.models.KMyMoneySimpleAccountTransactionsTableModel;

/**
 * (c) 2009 by <a href="http://Wolschon.biz>Wolschon Softwaredesign und Beratung</a>.<br/>
 * Project: jkmymoneyLib-GPL<br/>
 * KMyMoneyAccountTransactionsTableModel<br/>
 * created: 12.10.2009 <br/>
 *<br/><br/>
 * <b>Version  of KMyMoneySimpleAccountTransactionsTableModel that reacts to
 * updates</b>
 * @author  <a href="mailto:Marcus@Wolschon.biz">fox</a>
 */
public class KMyMoneyAccountTransactionsTableModel extends KMyMoneySimpleAccountTransactionsTableModel {
	//TODO: allow editing by the user of transaction-descriptions+dates and split-values for transactions with exactly 2 splits.

	/**
	 * Listener that we attach to the account and the displayed splits
	 * to be informed about changes.
	 */
	private final MyPropertyChangeListener myPropertyChangeListener = new MyPropertyChangeListener();
	/**
	 * The splits we have added our listener to.
	 */
	private final Set<KMyMoneyWritableTransactionSplit> myObservedSplits = new HashSet<KMyMoneyWritableTransactionSplit>();

	/**
	 * @param anAccount the account whos transactions to display
	 */
	public KMyMoneyAccountTransactionsTableModel(final KMyMoneyAccount anAccount) {
		super(anAccount);

		// inform our listeners about changes
		if (anAccount instanceof KMyMoneyWritableAccount) {
			KMyMoneyWritableAccount wacc = (KMyMoneyWritableAccount) anAccount;
			// ::TODO
			// wacc.addPropertyChangeListener("transactionSplits", myPropertyChangeListener);
		}
	}

	/**
	 * Empty  model.
	 */
	public KMyMoneyAccountTransactionsTableModel() {
		super();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object getValueAt(final int rowIndex, final int columnIndex) {
		KMyMoneyTransactionSplit split = getTransactionSplit(rowIndex);
		if (split instanceof KMyMoneyWritableTransactionSplit) {
			KMyMoneyWritableTransactionSplit ws = (KMyMoneyWritableTransactionSplit) split;
			if (!myObservedSplits.contains(ws)) {
				// ::TODO
				// ws.addPropertyChangeListener(myPropertyChangeListener);
				myObservedSplits.add(ws);
			}
		}
		return super.getValueAt(rowIndex, columnIndex);
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
			TableModelEvent event = new TableModelEvent(KMyMoneyAccountTransactionsTableModel.this);
			for (TableModelListener tableModelListener : listeners) {
				tableModelListener.tableChanged(event);
			}
		}
	}

}
