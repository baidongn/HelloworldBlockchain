package com.xingkaichun.helloworldblockchain.netcore.transport.dto;

/**
 * 未花费交易输出
 * 属性含义参考 com.xingkaichun.helloworldblockchain.core.model.TransactionInput
 *
 * @author 邢开春 微信HelloworldBlockchain 邮箱xingkaichun@qq.com
 */
public class UnspendTransactionOutputDTO {

    //交易的输入
    private String transactionHash;
    private long transactionOutputIndex;

    public String getTransactionHash() {
        return transactionHash;
    }

    public void setTransactionHash(String transactionHash) {
        this.transactionHash = transactionHash;
    }

    public long getTransactionOutputIndex() {
        return transactionOutputIndex;
    }

    public void setTransactionOutputIndex(long transactionOutputIndex) {
        this.transactionOutputIndex = transactionOutputIndex;
    }
}
