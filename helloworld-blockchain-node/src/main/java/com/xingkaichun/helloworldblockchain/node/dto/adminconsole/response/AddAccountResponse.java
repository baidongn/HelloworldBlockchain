package com.xingkaichun.helloworldblockchain.node.dto.adminconsole.response;

import com.xingkaichun.helloworldblockchain.crypto.model.Account;

/**
 * @author xingkaichun@ceair.com
 */
public class AddAccountResponse {

    private boolean addAccountSuccess ;

    private Account account;


    public boolean isAddAccountSuccess() {
        return addAccountSuccess;
    }

    public void setAddAccountSuccess(boolean addAccountSuccess) {
        this.addAccountSuccess = addAccountSuccess;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }
}
