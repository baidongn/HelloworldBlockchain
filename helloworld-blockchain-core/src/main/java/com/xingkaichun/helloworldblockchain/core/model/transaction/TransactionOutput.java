package com.xingkaichun.helloworldblockchain.core.model.transaction;


import com.xingkaichun.helloworldblockchain.core.model.script.ScriptLock;

import java.io.Serializable;

/**
 * 交易输出
 *
 * @author 邢开春 微信HelloworldBlockchain 邮箱xingkaichun@qq.com
 */
public class TransactionOutput extends TransactionOutputId implements Serializable {

    //交易输出的金额
    private long value;
    /**
     * 脚本锁
     * 交易输出不应该是任何用户都可以使用的，只有能证明这个交易输出属于该用户的用户才可以使用这个交易输出。
     * 如何证明用户拥有这个交易输出？
     * 这里我们给交易输出加上一把锁，自然拥有锁对应钥匙的用户可以使用这个交易输出。
     */
    private ScriptLock scriptLock;

    /**
     * 交易输出的地址
     * 冗余；可以从脚本锁解析出地址
     */
    private String address;

    /**
     * 交易所在区块的区块高度
     * 冗余
     */
    private long blockHeight;
    /**
     * 交易输出在的交易在所在的区块中的交易序列号
     * 冗余
     * 在这个交易区块中的的排序号
     */
    private long transactionSequenceNumberInBlock;




    //region get set

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

    public ScriptLock getScriptLock() {
        return scriptLock;
    }

    public void setScriptLock(ScriptLock scriptLock) {
        this.scriptLock = scriptLock;
    }

    public long getBlockHeight() {
        return blockHeight;
    }

    public void setBlockHeight(long blockHeight) {
        this.blockHeight = blockHeight;
    }

    public long getTransactionSequenceNumberInBlock() {
        return transactionSequenceNumberInBlock;
    }

    public void setTransactionSequenceNumberInBlock(long transactionSequenceNumberInBlock) {
        this.transactionSequenceNumberInBlock = transactionSequenceNumberInBlock;
    }

    //endregion
}
