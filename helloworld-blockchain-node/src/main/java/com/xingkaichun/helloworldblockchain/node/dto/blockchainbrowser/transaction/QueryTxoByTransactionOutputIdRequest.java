package com.xingkaichun.helloworldblockchain.node.dto.blockchainbrowser.transaction;

import com.xingkaichun.helloworldblockchain.core.model.transaction.TransactionOutputId;

public class QueryTxoByTransactionOutputIdRequest {

    TransactionOutputId transactionOutputId;


    public TransactionOutputId getTransactionOutputId() {
        return transactionOutputId;
    }

    public void setTransactionOutputId(TransactionOutputId transactionOutputId) {
        this.transactionOutputId = transactionOutputId;
    }
}
