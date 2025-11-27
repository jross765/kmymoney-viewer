package org.kmymoney.viewer.actions;

import javax.swing.Action;

import org.kmymoney.api.read.KMyMoneyAccount;

/**
 * Action that can be executed on a {@link KMyMoneyAccount}
 */
public interface AccountAction extends Action {

    // Set the account this action works on.
    void setAccount(final KMyMoneyAccount acct);
}
