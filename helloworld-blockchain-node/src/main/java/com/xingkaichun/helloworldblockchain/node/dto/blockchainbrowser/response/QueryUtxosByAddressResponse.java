package com.xingkaichun.helloworldblockchain.node.dto.blockchainbrowser.response;

import java.util.List;

/**
 *
 * @author 邢开春 微信HelloworldBlockchain 邮箱xingkaichun@qq.com
 */
public class QueryUtxosByAddressResponse {

    private List<TransactionOutputDto> utxos;




    //region get set

    public List<TransactionOutputDto> getUtxos() {
        return utxos;
    }

    public void setUtxos(List<TransactionOutputDto> utxos) {
        this.utxos = utxos;
    }


    //endregion

    public static class TransactionOutputDto {

        private long value;
        private String scriptLock;
        private long blockHeight;
        private String transactionHash;

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
    }
}
