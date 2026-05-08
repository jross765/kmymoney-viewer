package org.kmymoney.viewer;

import java.util.Locale;

import org.apache.commons.numbers.fraction.BigFraction;
import org.kmymoney.api.read.impl.KMyMoneyAccountImpl;

public class GUIServices
{
	
    // ---------------------------------------------------------------
	// Redundant wrappers for convenience.
	
	public static String formatBalance(KMyMoneyAccountImpl acct, BigFraction blnc) {
		return KMyMoneyAccountImpl.formatBalanceRat( acct, blnc );
	}
	
	public static String formatBalance(KMyMoneyAccountImpl acct, BigFraction blnc, Locale lcl) {
		return KMyMoneyAccountImpl.formatBalanceRat( acct, blnc, lcl );
	}
	
}
