package org.kmymoney.viewer.widgets;

import java.awt.BorderLayout;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.kmymoney.api.currency.ComplexPriceTable;
import org.kmymoney.api.read.KMyMoneyAccount;
import org.kmymoney.api.read.KMyMoneyFile;
import org.kmymoney.api.read.KMyMoneyTransaction;
import org.kmymoney.api.read.KMyMoneyTransactionSplit;
import org.kmymoney.base.basetypes.complex.KMMComplAcctID;
import org.kmymoney.base.basetypes.complex.KMMQualifSecCurrID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.numbers.FixedPointNumber;

/**
 * This panel displays a sum of all transaction-splits that are
 * to any of a list of accounts belonging to transactions with at
 * least one split in another list of accounts.<br/>
 * It is very handy for tax- and other reports.
 */
public class TransactionSum extends JPanel {

	/**
	 * For serializing.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Automatically created logger for debug and error-output.
	 */
	/**
	 * Our logger for debug- and error-ourput.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(TransactionSum.class);

	/**
	 * The financial data we operate on.
	 */
	private KMyMoneyFile myBooks;

	/**
	 * We only operate on transactions that
	 * contain one of these accounts.
	 */
	private Set<KMyMoneyAccount> mySourceAccounts;

	/**
	 * We sum all transaction-splits that are to
	 * one of these accounts.
	 *
	 * @see #mySourceAccounts
	 */
	private Set<KMyMoneyAccount> myTargetAccounts;

	/**
	 * We ignore all transactions that are before this date.
	 */
	private LocalDate myMinDate;
	/**
	 * We ignore all transactions that are after this date.
	 */
	private LocalDate myMaxDate;

	/**
	 * The type of summations we are to calculate.
	 */
	private SUMMATIONTYPE mySummationType = SUMMATIONTYPE.ALL;

	/**
	 * (c) 2007 by <a href="http://Wolschon.biz>
	 * Wolschon Softwaredesign und Beratung</a>.<br/>
	 * Project: jkmymoneyLib-V1<br/>
	 * TransactionSum.java<br/>
	 * created: 09.12.2007 12:34:53 <br/>
	 * <br/><br/>
	 * The types of summations we can do.
	 *
	 * @author <a href="mailto:Marcus@Wolschon.biz">Marcus Wolschon</a>
	 */
	public enum SUMMATIONTYPE {
		/**
		 * Sum all splits.
		 */
		ALL,
		/**
		 * Sum only splits that increase the balance on
		 * the targetAccount.
		 *
		 * @see TransactionSum#myTargetAccounts
		 */
		ONLYTO,
		/**
		 * Sum only splits that decrease the balance on
		 * the targetAccount.
		 *
		 * @see TransactionSum#myTargetAccounts
		 */
		ONLYFROM;

		/**
		 * @param aProperty parse this string
		 * @return and return the value that matches the name
		 */
		public static SUMMATIONTYPE getByName(final String aProperty) {
			if (aProperty.equalsIgnoreCase("all")) {
				return ALL;
			}

			if (aProperty.equalsIgnoreCase("to")) {
				return ONLYTO;
			}
			if (aProperty.equalsIgnoreCase("onlyto")) {
				return ONLYTO;
			}

			if (aProperty.equalsIgnoreCase("from")) {
				return ONLYFROM;
			}
			if (aProperty.equalsIgnoreCase("onlyfrom")) {
				return ONLYFROM;
			}
			if (aProperty.equalsIgnoreCase("allrecursive")) {
				return ALL;
			}

			return ALL;
		}
	}

	/**
	 * The label that contains the sum.
	 */
	private final JLabel mySumLabel = new JLabel();
	/**
	 * The label that contains the name
	 * to display left of the sum.
	 */
	private final JLabel myNameLabel = new JLabel();
	/**
	 * The label to display a link
	 * for a drilldown to the list of
	 * transactions covered.
	 */
	private final JLabel myDrilldownLabel = new JLabel();

	/**
	 * The latest value calculated by {@link #reCalculate()}.
	 */
	private FixedPointNumber myValue = null;

	/**
	 * The count of transactions we counted n the last {@link #reCalculate()}.
	 */
	private int myTransactionsCounted = -1;

	/**
	 * @param books          The financial data we operate on.
	 * @param summationType  The type of summations we are to calculate.
	 * @param targetAccounts We sum all transaction-splits that are to
	 *                       one of these accounts.
	 * @param sourceAccounts We only operate on transactions that
	 *                       contain one of these accounts.
	 * @param name           the name to display left of the sum
	 * @param minDate        We ignore all transactions that are before this date.
	 * @param maxDate        We ignore all transactions that are after this date.
	 */
	public TransactionSum(final KMyMoneyFile books,
			final Set<KMyMoneyAccount> sourceAccounts,
			final Set<KMyMoneyAccount> targetAccounts,
			final SUMMATIONTYPE summationType,
			final String name,
			final LocalDate minDate,
			final LocalDate maxDate) {
		initializeUI(name);
		setName(name);
		setBooks(books);
		setSummationType(summationType);
		setSourceAccounts(sourceAccounts);
		setTargetAccounts(targetAccounts);
		setMinDate(minDate);
		setMaxDate(maxDate);
	}

	/**
	 * Do the actual calculation.
	 */
	private void reCalculate() {
		if (getSummationType() == null
				|| getSourceAccounts() == null
				|| getTargetAccounts() == null
				|| getSummationType() == null
				//|| getMinDate() == null
				//|| getMaxDate() == null
				|| getBooks() == null) {
			mySumLabel.setText("---");
			return;
		}
		myTransactionsCounted = 0;

		Set<KMyMoneyAccount> sourceAccounts = new HashSet<KMyMoneyAccount>(
				getSourceAccounts());
		Set<KMyMoneyAccount> targetAccounts = new HashSet<KMyMoneyAccount>(
				buildTransitiveClosure(getTargetAccounts()));
		Set<KMMComplAcctID> targetAccountsIDs = new HashSet<KMMComplAcctID>();
		for (KMyMoneyAccount targetAccount : targetAccounts) {
			targetAccountsIDs.add(targetAccount.getID());
		}

		////////////////////////////////////
		// find all applicable transacion
		Set<KMyMoneyTransactionSplit> transactions = new HashSet<KMyMoneyTransactionSplit>();
		FixedPointNumber sum = new FixedPointNumber(0);
		if (sourceAccounts.size() == 0) {
			LOGGER.warn("There are no source-accounts given for this transaction-sum");
		}
		for (KMyMoneyAccount sourceAccount : sourceAccounts) {
			FixedPointNumber addMe =
					buildSum(sourceAccount,
							targetAccountsIDs,
							sourceAccount.getQualifSecCurrID(),
							transactions);
			if (addMe == null) {
				mySumLabel.setText("   cannot determine sum");
				sum = null;
				break;
			}
			sum = sum.add(addMe);
		}

		setValue(sum);
		////////////////////////////////////
		// set output
		Iterator<KMyMoneyAccount> iterator = targetAccounts.iterator();
		if (iterator.hasNext()) {
			mySumLabel.setText("   " + sum.toString() + ""
					+ iterator.next().getQualifSecCurrID());
		} else {
			Iterator<KMyMoneyAccount> iterator2 = sourceAccounts.iterator();
			if (iterator2.hasNext()) {
				mySumLabel.setText("   " + sum.toString() + ""
						+ iterator2.next().getQualifSecCurrID());
			} else {
				mySumLabel.setText("   no account");
			}
		}
	}

	/**
	 * @param alreadyHandled all transactions we have already visited (if multiple target-accounts are involved)
	 */
	private FixedPointNumber buildSum(final KMyMoneyAccount aSourceAccount,
			final Set<KMMComplAcctID> aTargetAccountsIDs,
			final KMMQualifSecCurrID currencyID,
			final Set<KMyMoneyTransactionSplit> alreadyHandled) {

		FixedPointNumber sum = new FixedPointNumber();
		for (Object element : aSourceAccount.getChildren()) {
			KMyMoneyAccount child = (KMyMoneyAccount) element;
			sum = sum.add(buildSum(child, aTargetAccountsIDs, currencyID, alreadyHandled));
		}

		List<? extends KMyMoneyTransactionSplit> splits
				= aSourceAccount.getTransactionSplits();
		for (KMyMoneyTransactionSplit split : splits) {
			KMyMoneyTransaction transaction = split.getTransaction();
			if (getMinDate() != null && transaction.getDatePosted().isBefore( getMinDate()) ) {
				continue;
			}
			if (getMaxDate() != null && transaction.getDatePosted().isAfter( getMaxDate()) ) {
				continue;
			}
			if (aTargetAccountsIDs.size() > 0 && !hasSplitWithAccount(transaction, aTargetAccountsIDs)) {
				continue;
			}
			if (alreadyHandled.contains(split)) {
				continue;
			}
			alreadyHandled.add(split);

			if (getSummationType().equals(SUMMATIONTYPE.ONLYFROM) && split.getShares().isPositive()) {
				continue;
			} else if (getSummationType().equals(SUMMATIONTYPE.ONLYTO) && !split.getShares().isPositive()) {
				continue;
			}
			if (aSourceAccount.getQualifSecCurrID().getType() == currencyID.getType()
					&& aSourceAccount.getQualifSecCurrID().equals(currencyID)) {

				sum = sum.add(split.getShares());
			} else {
				FixedPointNumber addMe = new FixedPointNumber(split.getShares());
				// do not convert 0
				if (!addMe.equals(new FixedPointNumber())) {
					addMe = convert(aSourceAccount.getQualifSecCurrID(), addMe, currencyID);
				}
				if (addMe == null) {
					return null;
				}
				sum = sum.add(addMe);
			}
			myTransactionsCounted++;

		}
		return sum;
	}

	/**
	 * @param aTransaction
	 * @param aTargetAccountsIDs
	 * @return
	 */
	private boolean hasSplitWithAccount(KMyMoneyTransaction aTransaction, Set<KMMComplAcctID> aTargetAccountsIDs) {
		List<? extends KMyMoneyTransactionSplit> splits = aTransaction.getSplits();
		for (KMyMoneyTransactionSplit split : splits) {
			if (aTargetAccountsIDs.contains(split.getAccountID())) {
				return true;
			}
		}
		return false;
	}

	private FixedPointNumber convert(
			final KMMQualifSecCurrID aCurrencyIDFrom,
			final FixedPointNumber aSum,
			final KMMQualifSecCurrID aCurrencyIDTo) {
		ComplexPriceTable currencyTable = getBooks().getCurrencyTable();

		if (currencyTable == null) {
			LOGGER.warn("SimpleAccount.getBalance() - cannot transfer "
					+ "to given currency because we have no currency-table!");
			return null;
		}
		FixedPointNumber sum = new FixedPointNumber(aSum);

		if (!currencyTable.convertToBaseCurrency(
				sum,
				aCurrencyIDFrom)) {
			Collection<String> currencies = getBooks().getCurrencyTable().getCurrencies(
					aCurrencyIDFrom.getType());
			LOGGER.warn("SimpleAccount.getBalance() - cannot transfer "
					+ "from our currency '"
					+ aCurrencyIDFrom.getType() + "'-'"
					+ aCurrencyIDFrom
					+ "' to the base-currency!"
					+ " \n(we know " + getBooks().getCurrencyTable().getNameSpaces().size()
					+ " currency-namespaces and "
					+ (currencies == null ? "no" : "" + currencies.size())
					+ " currencies in our namespace)");
			return null;
		}

		if (!currencyTable.convertFromBaseCurrency(sum, aCurrencyIDTo)) {
			LOGGER.warn("SimpleAccount.getBalance() - cannot transfer "
					+ "from base-currenty to given currency '"
					+ aCurrencyIDTo.getType()
					+ "-"
					+ aCurrencyIDTo
					+ "'!");
			return null;
		}
		return sum;
	}

	/**
	 * Build the transitive closure of a list of accounts
	 * by adding all child-accounts.
	 *
	 * @param accounts the account-list to walk
	 * @return a set of all given accounts and all their child-accounts.
	 */
	private Collection<? extends KMyMoneyAccount> buildTransitiveClosure(
			final Collection<? extends KMyMoneyAccount> accounts) {

		if (accounts.size() == 0) {
			return accounts;
		}

		Set<KMyMoneyAccount> retval = new HashSet<KMyMoneyAccount>(accounts);

		// TODO implement TransactionSum.buildTransitiveClosure
		for (KMyMoneyAccount account : accounts) {
			Collection<? extends KMyMoneyAccount> allChildren
					= buildTransitiveClosure(account.getChildren());
			retval.addAll(allChildren);
		}
		return retval;
	}

	/**
	 * Create the UI-components.
	 *
	 * @param name the name to display left of the sum
	 */
	public void initializeUI(final String name) {
		this.setLayout(new BorderLayout());
		myNameLabel.setText(name);
		mySumLabel.setText("...�");
		myDrilldownLabel.setText("");//TODO: implement drilldown
		this.add(myNameLabel, BorderLayout.WEST);
		this.add(mySumLabel, BorderLayout.CENTER);
		this.add(myDrilldownLabel, BorderLayout.EAST);
	}
	//------------------------ support for propertyChangeListeners -------------

	/**
	 * support for firing PropertyChangeEvents.
	 * (gets initialized only if we really have listeners)
	 */
	private volatile PropertyChangeSupport myPropertyChange = null;

	/**
	 * Returned value may be null if we never had listeners.
	 *
	 * @return Our support for firing PropertyChangeEvents
	 */
	protected final PropertyChangeSupport getPropertyChangeSupport() {
		return myPropertyChange;
	}

	/**
	 * Add a PropertyChangeListener to the listener list.
	 * The listener is registered for all properties.
	 *
	 * @param listener The PropertyChangeListener to be added
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
	 * @param propertyName The name of the property to listen on.
	 * @param listener     The PropertyChangeListener to be added
	 */
	@Override
	public final void addPropertyChangeListener(final String propertyName,
			final PropertyChangeListener listener) {
		if (myPropertyChange == null) {
			myPropertyChange = new PropertyChangeSupport(this);
		}
		myPropertyChange.addPropertyChangeListener(propertyName, listener);
	}

	/**
	 * Remove a PropertyChangeListener for a specific property.
	 *
	 * @param propertyName The name of the property that was listened on.
	 * @param listener     The PropertyChangeListener to be removed
	 */
	@Override
	public final void removePropertyChangeListener(final String propertyName,
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
	 * @param listener The PropertyChangeListener to be removed
	 */
	@Override
	public final synchronized void removePropertyChangeListener(
			final PropertyChangeListener listener) {
		if (myPropertyChange != null) {
			myPropertyChange.removePropertyChangeListener(listener);
		}
	}

	//-------------------------------------------------------

	/**
	 * @return Returns the books.
	 * @see #myBooks
	 */
	public KMyMoneyFile getBooks() {
		return myBooks;
	}

	/**
	 * @param aBooks The books to set.
	 * @see #myBooks
	 */
	public void setBooks(final KMyMoneyFile aBooks) {
		if (aBooks == null) {
			throw new IllegalArgumentException("null 'aBooks' given!");
		}

		Object old = myBooks;
		if (old == aBooks) {
			return; // nothing has changed
		}
		myBooks = aBooks;
		// <<insert code to react further to this change here
		reCalculate();
		PropertyChangeSupport propertyChangeFirer = getPropertyChangeSupport();
		if (propertyChangeFirer != null) {
			propertyChangeFirer.firePropertyChange("aBooks", old, aBooks);
		}
	}

	/**
	 * @return Returns the sourceAccounts.
	 * @see #mySourceAccounts
	 */
	public Set<KMyMoneyAccount> getSourceAccounts() {
		return mySourceAccounts;
	}

	/**
	 * @param aSourceAccounts The sourceAccounts to set.
	 * @see #mySourceAccounts
	 */
	public void setSourceAccounts(final Set<KMyMoneyAccount> aSourceAccounts) {
		if (aSourceAccounts == null) {
			throw new IllegalArgumentException("null 'aSourceAccounts' given!");
		}

		Object old = mySourceAccounts;
		if (old == aSourceAccounts) {
			return; // nothing has changed
		}
		mySourceAccounts = aSourceAccounts;
		// <<insert code to react further to this change here
		reCalculate();
		PropertyChangeSupport propertyChangeFirer = getPropertyChangeSupport();
		if (propertyChangeFirer != null) {
			propertyChangeFirer.firePropertyChange("aSourceAccounts", old,
					aSourceAccounts);
		}
	}

	/**
	 * @return Returns the summationType.
	 * @see #mySummationType
	 */
	public SUMMATIONTYPE getSummationType() {
		return mySummationType;
	}

	/**
	 * @param aSummationType The summationType to set.
	 * @see #mySummationType
	 */
	public void setSummationType(final SUMMATIONTYPE aSummationType) {
		if (aSummationType == null) {
			throw new IllegalArgumentException("null 'aSummationType' given!");
		}

		Object old = mySummationType;
		if (old == aSummationType) {
			return; // nothing has changed
		}
		mySummationType = aSummationType;
		// <<insert code to react further to this change here
		reCalculate();
		PropertyChangeSupport propertyChangeFirer = getPropertyChangeSupport();
		if (propertyChangeFirer != null) {
			propertyChangeFirer.firePropertyChange("aSummationType", old,
					aSummationType);
		}
	}

	/**
	 * @return Returns the targetAccounts.
	 * @see #myTargetAccounts
	 */
	public Set<KMyMoneyAccount> getTargetAccounts() {
		return myTargetAccounts;
	}

	/**
	 * @param aTargetAccounts The targetAccounts to set.
	 * @see #myTargetAccounts
	 */
	public void setTargetAccounts(final Set<KMyMoneyAccount> aTargetAccounts) {
		if (aTargetAccounts == null) {
			throw new IllegalArgumentException("null 'aTargetAccounts' given!");
		}

		Object old = myTargetAccounts;
		if (old == aTargetAccounts) {
			return; // nothing has changed
		}
		myTargetAccounts = aTargetAccounts;
		// <<insert code to react further to this change here
		reCalculate();
		PropertyChangeSupport propertyChangeFirer = getPropertyChangeSupport();
		if (propertyChangeFirer != null) {
			propertyChangeFirer.firePropertyChange("aTargetAccounts", old,
					aTargetAccounts);
		}
	}

	/**
	 * @return Returns the minDate.
	 * @see #myMinDate
	 */
	public LocalDate getMinDate() {
		return myMinDate;
	}

	/**
	 * @param aMinDate The minDate to set.
	 * @see #myMinDate
	 */
	public void setMinDate(final LocalDate aMinDate) {
		//        if (aMinDate == null) {
		//            throw new IllegalArgumentException("null 'aMinDate' given!");
		//        }

		Object old = myMinDate;
		if (old == aMinDate) {
			return; // nothing has changed
		}
		myMinDate = aMinDate;
		// <<insert code to react further to this change here
		reCalculate();
		PropertyChangeSupport propertyChangeFirer = getPropertyChangeSupport();
		if (propertyChangeFirer != null) {
			propertyChangeFirer.firePropertyChange("aMinDate", old, aMinDate);
		}
	}

	/**
	 * @return Returns the maxDate.
	 * @see #myMaxDate
	 */
	public LocalDate getMaxDate() {
		return myMaxDate;
	}

	/**
	 * @param aMaxDate The maxDate to set.
	 * @see #myMaxDate
	 */
	public void setMaxDate(final LocalDate aMaxDate) {
		//        if (aMaxDate == null) {
		//            throw new IllegalArgumentException("null 'aMaxDate' given!");
		//        }

		Object old = myMaxDate;
		if (old == aMaxDate) {
			return; // nothing has changed
		}
		myMaxDate = aMaxDate;
		// <<insert code to react further to this change here
		reCalculate();
		PropertyChangeSupport propertyChangeFirer = getPropertyChangeSupport();
		if (propertyChangeFirer != null) {
			propertyChangeFirer.firePropertyChange("aMaxDate", old, aMaxDate);
		}
	}

	/**
	 * @return the value
	 */
	public FixedPointNumber getValue() {
		return myValue;
	}

	/**
	 * @param aValue the value to set
	 */
	private void setValue(final FixedPointNumber aValue) {
		myValue = aValue;
	}

	/**
	 * @return the transactionsCounted
	 */
	public int getTransactionsCounted() {
		return myTransactionsCounted;
	}
}


