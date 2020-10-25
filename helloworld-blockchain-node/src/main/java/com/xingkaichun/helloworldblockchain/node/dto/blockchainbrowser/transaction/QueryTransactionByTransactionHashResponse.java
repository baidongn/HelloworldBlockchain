package com.xingkaichun.helloworldblockchain.node.dto.blockchainbrowser.transaction;

import java.util.List;

/**
 *
 * @author 邢开春 微信HelloworldBlockchain 邮箱xingkaichun@qq.com
 */
public class QueryTransactionByTransactionHashResponse {

    private TransactionDto transactionDto;




    //region get set

    public TransactionDto getTransactionDto() {
        return transactionDto;
    }

    public void setTransactionDto(TransactionDto transactionDto) {
        this.transactionDto = transactionDto;
    }


    //endregion


    public static class TransactionDto {
        private String transactionHash;
        private long blockHeight;
        private long confirmCount;
        private String blockTime;

        private long transactionFee;
        private String transactionType;
        private long transactionInputCount;
        private long transactionOutputCount;
        private long transactionInputValues;
        private long transactionOutputValues;

        private List<TransactionInputDto> transactionInputDtoList;
        private List<TransactionOutputDto> transactionOutputDtoList;

        public String getTransactionHash() {
            return transactionHash;
        }

        public void setTransactionHash(String transactionHash) {
            this.transactionHash = transactionHash;
        }

        public long getBlockHeight() {
            return blockHeight;
        }

        public void setBlockHeight(long blockHeight) {
            this.blockHeight = blockHeight;
        }

        public long getConfirmCount() {
            return confirmCount;
        }

        public void setConfirmCount(long confirmCount) {
            this.confirmCount = confirmCount;
        }

        public String getBlockTime() {
            return blockTime;
        }

        public void setBlockTime(String blockTime) {
            this.blockTime = blockTime;
        }

        public long getTransactionFee() {
            return transactionFee;
        }

        public void setTransactionFee(long transactionFee) {
            this.transactionFee = transactionFee;
        }

        public String getTransactionType() {
            return transactionType;
        }

        public void setTransactionType(String transactionType) {
            this.transactionType = transactionType;
        }

        public long getTransactionInputCount() {
            return transactionInputCount;
        }

        public void setTransactionInputCount(long transactionInputCount) {
            this.transactionInputCount = transactionInputCount;
        }

        public long getTransactionOutputCount() {
            return transactionOutputCount;
        }

        public void setTransactionOutputCount(long transactionOutputCount) {
            this.transactionOutputCount = transactionOutputCount;
        }

        public long getTransactionInputValues() {
            return transactionInputValues;
        }

        public void setTransactionInputValues(long transactionInputValues) {
            this.transactionInputValues = transactionInputValues;
        }

        public long getTransactionOutputValues() {
            return transactionOutputValues;
        }

        public void setTransactionOutputValues(long transactionOutputValues) {
            this.transactionOutputValues = transactionOutputValues;
        }

        public List<TransactionInputDto> getTransactionInputDtoList() {
            return transactionInputDtoList;
        }

        public void setTransactionInputDtoList(List<TransactionInputDto> transactionInputDtoList) {
            this.transactionInputDtoList = transactionInputDtoList;
        }

        public List<TransactionOutputDto> getTransactionOutputDtoList() {
            return transactionOutputDtoList;
        }

        public void setTransactionOutputDtoList(List<TransactionOutputDto> transactionOutputDtoList) {
            this.transactionOutputDtoList = transactionOutputDtoList;
        }
    }

    public static class TransactionInputDto {
        private String address;
        private long value;
        private String scriptKey;
        private String transactionHash;
        private long transactionOutputSequence;

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public long getValue() {
            return value;
        }

        public void setValue(long value) {
            this.value = value;
        }

        public String getScriptKey() {
            return scriptKey;
        }

        public void setScriptKey(String scriptKey) {
            this.scriptKey = scriptKey;
        }

        public String getTransactionHash() {
            return transactionHash;
        }

        public void setTransactionHash(String transactionHash) {
            this.transactionHash = transactionHash;
        }

        public long getTransactionOutputSequence() {
            return transactionOutputSequence;
        }

        public void setTransactionOutputSequence(long transactionOutputSequence) {
            this.transactionOutputSequence = transactionOutputSequence;
        }
    }

    public static class TransactionOutputDto {
        private String address;
        private long value;
        private String scriptLock;
        private String transactionHash;
        private long transactionOutputSequence;

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
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

        public String getTransactionHash() {
            return transactionHash;
        }

        public void setTransactionHash(String transactionHash) {
            this.transactionHash = transactionHash;
        }

        public long getTransactionOutputSequence() {
            return transactionOutputSequence;
        }

        public void setTransactionOutputSequence(long transactionOutputSequence) {
            this.transactionOutputSequence = transactionOutputSequence;
        }
    }

}
