package com.xingkaichun.helloworldblockchain.core.tools;

import com.xingkaichun.helloworldblockchain.core.BlockChainCore;
import com.xingkaichun.helloworldblockchain.core.model.transaction.TransactionOutput;

import java.util.List;

public class WalletTool {

    public static long obtainBalance(BlockChainCore blockChainCore, String privateKey) {
        //交易输出总金额
        long totalValue = 0;
        long from = 0;
        long size = 100;
        while(true){
            List<TransactionOutput> utxoList = blockChainCore.getBlockChainDataBase().queryUnspendTransactionOutputListByAddress(privateKey,from,size);
            if(utxoList == null || utxoList.size()==0){
                break;
            }
            for(TransactionOutput transactionOutput:utxoList){
                totalValue += transactionOutput.getValue();
            }
            from += size;
        }
        return totalValue;
    }
}
