package com.xingkaichun.helloworldblockchain.node.dto.blockchainbrowser.transaction;

public class QueryTxoByTransactionOutputIdResponse {

    private TransactionOutputDto transactionOutputDto;

    public TransactionOutputDto getTransactionOutputDto() {
        return transactionOutputDto;
    }

    public void setTransactionOutputDto(TransactionOutputDto transactionOutputDto) {
        this.transactionOutputDto = transactionOutputDto;
    }

    public static class TransactionOutputDto {

        private long value;
        private String scriptLock;
        private String scriptKey;
        private long blockHeight;
        private String transactionHash;
        private long transactionOutputIndex;
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

        public String getScriptKey() {
            return scriptKey;
        }

        public void setScriptKey(String scriptKey) {
            this.scriptKey = scriptKey;
        }

        public long getTransactionOutputIndex() {
            return transactionOutputIndex;
        }

        public void setTransactionOutputIndex(long transactionOutputIndex) {
            this.transactionOutputIndex = transactionOutputIndex;
        }
    }
}
