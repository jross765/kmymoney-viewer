/**
 * OpenAccountInNewWindow.java
 * created: 12.03.2009
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

package org.kmymoney.jkmymoney.actions;


import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

import org.kmymoney.api.read.KMyMoneyAccount;
import org.kmymoney.api.read.KMyMoneyTransaction;
import org.kmymoney.api.read.KMyMoneyTransactionSplit;
import org.kmymoney.jkmymoney.panels.WritableTransactionsPanel;
import org.kmymoney.viewer.actions.AccountAction;
import org.kmymoney.viewer.actions.TransactionSplitAction;

/**
 * (c) 2009 by <a href="http://Wolschon.biz>Wolschon Softwaredesign und Beratung</a>.<br/>
 * Project: jkmymoneyLib-GPL<br/>
 * OpenAccountInNewWindow<br/>
 * created: 12.03.2009 <br/>
 *<br/><br/>
 * <b>Action to open an account in a new tab.</b>
 * @author  <a href="mailto:Marcus@Wolschon.biz">Marcus Wolschon</a>
 */
public class OpenAccountInNewWindowWritable implements AccountAction, TransactionSplitAction {

	/**
	 * The account we open.
	 */
	private KMyMoneyAccount myAccount;

	/**
	 * @see #getValue(String)
	 */
	private final Map<String, Object> myAddedTags = new HashMap<String, Object>();

	/**
	 * @see #addPropertyChangeListener(PropertyChangeListener)
	 */
	private final PropertyChangeSupport myPropertyChangeSupport = new PropertyChangeSupport(this);

	/**
	 * Optional the transaction to highlight.
	 */
	private KMyMoneyTransaction myTransaction;

	/**
	 * Initialize.
	 */
	public OpenAccountInNewWindowWritable() {
		this.putValue(Action.NAME, "Open Account in new Window");
		this.putValue(Action.LONG_DESCRIPTION, "Open the given Account in new Window.");
		this.putValue(Action.SHORT_DESCRIPTION, "Open Account in new Window.");
	}

	/**
	 * @param aSplit the split to show the account of.
	 */
	public OpenAccountInNewWindowWritable(final KMyMoneyTransactionSplit aSplit) {
		this();
		setSplit(aSplit);
	}

	/**
	 * @param anAccount the account to show.
	 */
	public OpenAccountInNewWindowWritable(final KMyMoneyAccount anAccount) {
		this();
		setAccount(anAccount);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setAccount(final KMyMoneyAccount anAccount) {
		myAccount = anAccount;
		myTransaction = null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setSplit(final KMyMoneyTransactionSplit aSplit) {
		myAccount = aSplit.getAccount();
		myTransaction = aSplit.getTransaction();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addPropertyChangeListener(final PropertyChangeListener aListener) {
		myPropertyChangeSupport.addPropertyChangeListener(aListener);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object getValue(final String aKey) {
		return myAddedTags.get(aKey);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isEnabled() {
		return myAccount != null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void putValue(final String aKey, final Object aValue) {
		myAddedTags.put(aKey, aValue);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removePropertyChangeListener(final PropertyChangeListener aListener) {
		myPropertyChangeSupport.removePropertyChangeListener(aListener);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setEnabled(final boolean aB) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void actionPerformed(final ActionEvent aE) {
		WritableTransactionsPanel newPanel = new WritableTransactionsPanel();
		newPanel.setAccount(myAccount);
		if (myTransaction != null) {
			newPanel.setTransaction(myTransaction);
		}
		JFrame newFrame = new JFrame(myAccount.getName());
		newFrame.getContentPane().add(newPanel);
		newFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		newFrame.pack();
		newFrame.setVisible(true);
	}


}
