package com.xingkaichun.helloworldblockchain.core;

import com.xingkaichun.helloworldblockchain.crypto.model.Account;

import java.util.List;

/**
 * @author xingkaichun@ceair.com
 */
public abstract class Wallet {

    public abstract List<Account> queryAllAccount();

    public abstract Account createAccount();

    public abstract void addAccount(Account account);

    public abstract void deleteAccountByAddress(String address);
}
