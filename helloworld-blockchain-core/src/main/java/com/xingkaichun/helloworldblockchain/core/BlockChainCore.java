package com.xingkaichun.helloworldblockchain.core;

import com.xingkaichun.helloworldblockchain.core.model.Block;
import com.xingkaichun.helloworldblockchain.core.model.pay.Recipient;
import com.xingkaichun.helloworldblockchain.core.model.transaction.Transaction;
import com.xingkaichun.helloworldblockchain.core.model.transaction.TransactionOutput;
import com.xingkaichun.helloworldblockchain.netcore.transport.dto.TransactionDTO;

import java.util.List;

/**
 * 单机版[没有网络交互版本]区块链核心，代表一个完整的单机版区块链核心系统。
 * 单机版区块链核心系统，由以下几部分组成：
 * 区块链数据库：用于持久化本地区块链的数据
 * @see com.xingkaichun.helloworldblockchain.core.BlockChainDataBase
 * 矿工：可以收集交易，挖矿，将新挖取的矿放进区块链数据库
 * @see com.xingkaichun.helloworldblockchain.core.Miner
 * 区块链同步器：区块链是一个分布式的数据库，同步器可以同步其它节点的区块链数据。
 * @see com.xingkaichun.helloworldblockchain.core.Synchronizer
 *
 * @author 邢开春 微信HelloworldBlockchain 邮箱xingkaichun@qq.com
 */
public abstract class BlockChainCore {

    //区块链数据库
    protected BlockChainDataBase blockChainDataBase ;
    //矿工
    protected Miner miner ;
    //钱包
    protected Wallet wallet ;
    //区块链同步器
    protected Synchronizer synchronizer ;

    public BlockChainCore(BlockChainDataBase blockChainDataBase, Wallet wallet, Miner miner, Synchronizer synchronizer) {
        this.blockChainDataBase = blockChainDataBase;
        this.wallet = wallet;
        this.miner = miner;
        this.synchronizer = synchronizer;
    }

    /**
     * 激活区块链核心。激活矿工、激活区块链同步器。
     */
    public abstract void start();



    /**
     * 获取区块链高度
     */
    public abstract long queryBlockChainHeight() ;
    /**
     * 删除区块高度大于等于@blockHeight@的区块
     */
    public abstract void removeBlocksUtilBlockHeightLessThan(long blockHeight) ;
    /**
     * 根据区块高度获取区块Hash
     */
    public abstract String queryBlockHashByBlockHeight(long blockHeight) ;



    /**
     * 根据区块高度查询区块
     */
    public abstract Block queryBlockByBlockHeight(long blockHeight);
    /**
     * 根据区块哈希查询区块
     */
    public abstract Block queryBlockDtoByBlockHash(String blockHash);



    /**
     * 根据交易哈希获取交易
     */
    public abstract Transaction queryTransactionByTransactionHash(String transactionHash) ;
    /**
     * 根据交易高度获取交易
     */
    public abstract List<Transaction> queryTransactionByTransactionHeight(long from,long size) ;
    /**
     * 根据地址获取[未花费交易输出列表]
     */
    public abstract List<TransactionOutput> queryUtxoListByAddress(String address,long from,long size) ;
    /**
     * 根据地址获取[交易输出列表]
     */
    public abstract List<TransactionOutput> queryTxoListByAddress(String address,long from,long size) ;



    /**
     * 构建交易
     */
    public abstract TransactionDTO buildTransactionDTO(List<Recipient> recipientList) ;
    /**
     * 提交交易到区块链
     */
    public abstract void submitTransaction(TransactionDTO transactionDTO) ;
    /**
     * 查询挖矿中的交易
     */
    public abstract List<TransactionDTO> queryMiningTransactionList(long from,long size) ;
    /**
     * 根据交易哈希查询挖矿中的交易
     */
    public abstract TransactionDTO queryMiningTransactionDtoByTransactionHash(String transactionHash) ;






    //region get set
    public BlockChainDataBase getBlockChainDataBase() {
        return blockChainDataBase;
    }

    public Miner getMiner() {
        return miner;
    }

    public Synchronizer getSynchronizer() {
        return synchronizer;
    }

    public Wallet getWallet() {
        return wallet;
    }

    //endregion
}