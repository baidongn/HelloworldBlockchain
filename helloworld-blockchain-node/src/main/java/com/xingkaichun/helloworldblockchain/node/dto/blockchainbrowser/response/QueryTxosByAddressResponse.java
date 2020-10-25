package com.xingkaichun.helloworldblockchain.node.dto.blockchainbrowser.response;

import java.util.List;

/**
 *
 * @author 邢开春 微信HelloworldBlockchain 邮箱xingkaichun@qq.com
 */
public class QueryTxosByAddressResponse {

    private List<TransactionOutputDto> txos;




    //region get set

    public List<TransactionOutputDto> getTxos() {
        return txos;
    }

    public void setTxos(List<TransactionOutputDto> txos) {
        this.txos = txos;
    }


    //endregion



    public static class TransactionOutputDto {

        private long value;
        private String scriptLock;
        private long blockHeight;
        private String transactionHash;
        private boolean isSpend;
        private String destinationTransactionHash;

        public boolean isSpend() {
            return isSpend;
        }

        public void setSpend(boolean spend) {
            isSpend = spend;
        }

        public long getValue() {
            return value;
        }

        public void setValue(long value) {
            this.value = value;
        }

        public String getScriptLock() {
            return scriptLock;
        }

        public void setScriptLock(String scriptLock) {
            this.scriptLock = scriptLock;
        }

        public long getBlockHeight() {
            return blockHeight;
        }

        public void setBlockHeight(long blockHeight) {
            this.blockHeight = blockHeight;
        }

        public String getTransactionHash() {
            return transactionHash;
        }

        public void setTransactionHash(String transactionHash) {
            this.transactionHash = transactionHash;
        }

        public String getDestinationTransactionHash() {
            return destinationTransactionHash;
        }

        public void setDestinationTransactionHash(String destinationTransactionHash) {
            this.destinationTransactionHash = destinationTransactionHash;
        }
    }
}
