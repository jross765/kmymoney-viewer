package org.kmymoney.viewer.models;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;

import org.kmymoney.api.generated.ACCOUNT;
import org.kmymoney.api.read.KMyMoneyAccount;
import org.kmymoney.api.read.KMyMoneyFile;
import org.kmymoney.api.read.impl.KMyMoneyAccountImpl;

/**
 * Hack: By introducing this class with a re-implemented getRootAccount() method,
 * we have a dummy root element that is needed by the GUI.
 * (KMyMoney internally does not have a root account, as opposed to GnuCash).
 */
public class KMyMoneyFileImpl extends org.kmymoney.api.read.impl.KMyMoneyFileImpl
{
	private static String DUMMY_ROOT_ACCT_ID = "A000000";

	// ---------------------------------------------------------------

	public KMyMoneyFileImpl(File pFile) throws IOException
	{
		super( pFile );
	}

	public KMyMoneyFileImpl(KMyMoneyFile kmmFile) throws IOException
	{
		super( kmmFile.getFile() );
	}
	
	// ---------------------------------------------------------------

	@Override
    public KMyMoneyAccount getRootAccount() {
		ACCOUNT jwsdpDummyRoot = new ACCOUNT();
		jwsdpDummyRoot.setId(DUMMY_ROOT_ACCT_ID);
		jwsdpDummyRoot.setName("DUMMY ROOT");
		jwsdpDummyRoot.setDescription("Dummy root element");
		jwsdpDummyRoot.setType( BigInteger.ZERO );
		
		KMyMoneyAccount dummyRoot = new KMyMoneyAccountImpl(jwsdpDummyRoot, getKMyMoneyFile());

		for ( KMyMoneyAccount topLevAcct : getTopAccounts() ) {
			((KMyMoneyAccountImpl) topLevAcct).getJwsdpPeer()
				.setParentaccount( DUMMY_ROOT_ACCT_ID );
		}
    	
    	return dummyRoot;
    }

}
