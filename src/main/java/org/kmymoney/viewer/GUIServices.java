package org.kmymoney.viewer;

import java.util.Locale;

import org.kmymoney.api.read.impl.KMyMoneyAccountImpl;

import xyz.schnorxoborx.base.numbers.FixedPointNumber;

public class GUIServices
{
	
    // ---------------------------------------------------------------
	// Redundant wrappers for convenience.
	
	public static String formatBalance(KMyMoneyAccountImpl acct, FixedPointNumber blnc) {
		return KMyMoneyAccountImpl.formatBalance( acct, blnc );
	}
	
	public static String formatBalance(KMyMoneyAccountImpl acct, FixedPointNumber blnc, Locale lcl) {
		return KMyMoneyAccountImpl.formatBalance( acct, blnc, lcl );
	}
	
}
