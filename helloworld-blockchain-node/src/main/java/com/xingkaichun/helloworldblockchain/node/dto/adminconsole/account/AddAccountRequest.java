package com.xingkaichun.helloworldblockchain.node.dto.adminconsole.account;

/**
 * @author xingkaichun@ceair.com
 */
public class AddAccountRequest {

    String privateKey;


    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }
}
