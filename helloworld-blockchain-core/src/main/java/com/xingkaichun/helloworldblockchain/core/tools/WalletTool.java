package com.xingkaichun.helloworldblockchain.core.tools;

import com.xingkaichun.helloworldblockchain.core.BlockChainDataBase;
import com.xingkaichun.helloworldblockchain.core.model.transaction.TransactionOutput;

import java.util.List;

public class WalletTool {

    public static long totalUnspendTransactionOutputValues(BlockChainDataBase blockChainDataBase, String privateKey) {
        //TODO size可能不止10000
        List<TransactionOutput> utxoList = blockChainDataBase.queryUnspendTransactionOutputListByAddress(privateKey,0,10000);
        //交易输入总金额
        long inputValues = 0;
        for(TransactionOutput transactionOutput:utxoList){
            inputValues += transactionOutput.getValue();
        }
        return inputValues;
    }
}
