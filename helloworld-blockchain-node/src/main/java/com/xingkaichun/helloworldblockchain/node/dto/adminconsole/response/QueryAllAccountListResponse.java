package com.xingkaichun.helloworldblockchain.node.dto.adminconsole.response;

import com.xingkaichun.helloworldblockchain.crypto.model.Account;

import java.util.List;

/**
 * @author xingkaichun@ceair.com
 */
public class QueryAllAccountListResponse {


    private List<Account> accountList;

    public List<Account> getAccountList() {
        return accountList;
    }

    public void setAccountList(List<Account> accountList) {
        this.accountList = accountList;
    }
}
