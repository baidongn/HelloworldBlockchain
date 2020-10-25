package com.xingkaichun.helloworldblockchain.netcore.dto.transaction;

import com.xingkaichun.helloworldblockchain.core.model.pay.Recipient;

import java.util.List;

/**
 *
 * @author 邢开春 微信HelloworldBlockchain 邮箱xingkaichun@qq.com
 */
public class SubmitTransaction {

    private List<String> privateKeyList ;
    private List<Recipient> recipientList ;




    //region get set

    public List<String> getPrivateKeyList() {
        return privateKeyList;
    }

    public void setPrivateKeyList(List<String> privateKeyList) {
        this.privateKeyList = privateKeyList;
    }

    public List<Recipient> getRecipientList() {
        return recipientList;
    }

    public void setRecipientList(List<Recipient> recipientList) {
        this.recipientList = recipientList;
    }

    //endregion
}
