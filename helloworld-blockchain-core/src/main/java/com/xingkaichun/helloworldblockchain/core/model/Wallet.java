package com.xingkaichun.helloworldblockchain.core.model;

import com.xingkaichun.helloworldblockchain.crypto.model.Account;

import java.util.List;

public class Wallet {

    private List<Account> accountList;

    public List<Account> getAccountList() {
        return accountList;
    }

    public void setAccountList(List<Account> accountList) {
        this.accountList = accountList;
    }
}
