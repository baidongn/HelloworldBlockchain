package com.xingkaichun.helloworldblockchain.core.model.transaction;

import java.io.Serializable;

public class TransactionOutputId implements Serializable {
    /**
     * 交易哈希
     * 冗余
     */
    private String transactionHash;
    /**
     * 交易输出序列号
     * 冗余
     * 在这个交易中的的排序号
     */
    private long transactionOutputSequence;


    public String getTransactionOutputId() {
        return transactionHash + "|" + transactionOutputSequence;
    }

    public long getTransactionOutputSequence() {
        return transactionOutputSequence;
    }

    public void setTransactionOutputSequence(long transactionOutputSequence) {
        this.transactionOutputSequence = transactionOutputSequence;
    }

    public String getTransactionHash() {
        return transactionHash;
    }

    public void setTransactionHash(String transactionHash) {
        this.transactionHash = transactionHash;
    }
}
