package com.xingkaichun.helloworldblockchain.core.impl;

import com.google.common.primitives.Bytes;
import com.xingkaichun.helloworldblockchain.core.BlockChainDataBase;
import com.xingkaichun.helloworldblockchain.core.Consensus;
import com.xingkaichun.helloworldblockchain.core.Incentive;
import com.xingkaichun.helloworldblockchain.core.model.Block;
import com.xingkaichun.helloworldblockchain.core.model.enums.BlockChainActionEnum;
import com.xingkaichun.helloworldblockchain.core.model.transaction.*;
import com.xingkaichun.helloworldblockchain.core.tools.*;
import com.xingkaichun.helloworldblockchain.core.utils.EncodeDecodeUtil;
import com.xingkaichun.helloworldblockchain.core.utils.LevelDBUtil;
import com.xingkaichun.helloworldblockchain.core.utils.LongUtil;
import com.xingkaichun.helloworldblockchain.setting.GlobalSetting;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBIterator;
import org.iq80.leveldb.WriteBatch;
import org.iq80.leveldb.impl.WriteBatchImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


/**
 * 区块链
 *
 * 注意这是一个线程不安全的实现。在并发的情况下，不保证功能的正确性。
 * @author 邢开春 微信HelloworldBlockchain 邮箱xingkaichun@qq.com
 */
public class BlockChainDataBaseDefaultImpl extends BlockChainDataBase {

    //region 变量与构造函数
    private static final Logger logger = LoggerFactory.getLogger(BlockChainDataBaseDefaultImpl.class);

    private static final String BLOCKCHAIN_DATABASE_DIRECT_NAME = "BlockChainDataBase";
    //区块链数据库
    private DB blockChainDB;

    /**
     * 锁:保证对区块链增区块、删区块的操作是同步的。
     * 查询区块操作不需要加锁，原因是，只有对区块链进行区块的增删才会改变区块链的数据。
     */
    private ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    public BlockChainDataBaseDefaultImpl(String blockchainDataPath,Incentive incentive,Consensus consensus) {
        super(consensus,incentive);
        File blockChainDBFile = new File(blockchainDataPath,BLOCKCHAIN_DATABASE_DIRECT_NAME);
        this.blockChainDB = LevelDBUtil.createDB(blockChainDBFile);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LevelDBUtil.closeDB(blockChainDB);
        }));
    }
    //endregion



    //region 区块增加与删除
    @Override
    public boolean addBlock(Block block) {
        Lock writeLock = readWriteLock.writeLock();
        writeLock.lock();
        try{
            boolean isBlockCanAddToBlockChain = isBlockCanAddToBlockChain(block);
            if(!isBlockCanAddToBlockChain){
                return false;
            }
            WriteBatch writeBatch = createWriteBatch(block,BlockChainActionEnum.ADD_BLOCK);
            LevelDBUtil.write(blockChainDB,writeBatch);
            return true;
        }finally {
            writeLock.unlock();
        }
    }
    @Override
    public void removeTailBlock() {
        Lock writeLock = readWriteLock.writeLock();
        writeLock.lock();
        try{
            Block tailBlock = queryTailBlock();
            if(tailBlock == null){
                return;
            }
            WriteBatch writeBatch = createWriteBatch(tailBlock,BlockChainActionEnum.DELETE_BLOCK);
            LevelDBUtil.write(blockChainDB,writeBatch);
        }finally {
            writeLock.unlock();
        }
    }
    @Override
    public void removeBlocksUtilBlockHeightLessThan(long blockHeight) {
        Lock writeLock = readWriteLock.writeLock();
        writeLock.lock();
        try{
            while (true){
                Block tailBlock = queryTailBlock();
                if(tailBlock == null){
                    return;
                }
                if(LongUtil.isLessThan(tailBlock.getHeight(),blockHeight)){
                    return;
                }
                WriteBatch writeBatch = createWriteBatch(tailBlock,BlockChainActionEnum.DELETE_BLOCK);
                LevelDBUtil.write(blockChainDB,writeBatch);
            }
        }finally {
            writeLock.unlock();
        }
    }
    //endregion



    //region 校验区块、交易
    @Override
    public boolean isBlockCanAddToBlockChain(Block block) {
        //检查系统版本是否支持
        if(!GlobalSetting.SystemVersionConstant.isVersionLegal(block.getTimestamp())){
            logger.debug("系统版本过低，不支持校验区块，请尽快升级系统。");
            return false;
        }

        //校验区块的结构
        if(!StructureSizeTool.isBlockStructureLegal(block)){
            logger.debug("区块数据异常，请校验区块的结构。");
            return false;
        }
        //校验区块的存储容量
        if(!StructureSizeTool.isBlockStorageCapacityLegal(block)){
            logger.debug("区块数据异常，请校验区块的大小。");
            return false;
        }

        Block previousBlock = queryTailBlock();
        //校验区块写入的属性值
        if(!BlockPropertyTool.isWritePropertiesRight(previousBlock,block)){
            logger.debug("区块校验失败：区块的属性写入值与实际计算结果不一致。");
            return false;
        }

        //校验业务
        //校验区块时间
        if(!BlockTool.isBlockTimestampLegal(previousBlock,block)){
            logger.debug("区块生成的时间太滞后。");
            return false;
        }
        //新产生的哈希是否合法
        if(!isNewHashLegal(block)){
            logger.debug("区块数据异常，区块中新产生的哈希异常。");
            return false;
        }
        //双花校验
        if(isDoubleSpendAttackHappen(block)){
            logger.debug("区块数据异常，检测到双花攻击。");
            return false;
        }
        //校验共识
        if(!isReachConsensus(block)){
            logger.debug("区块数据异常，未满足共识规则。");
            return false;
        }
        //校验激励
        if(!isIncentiveRight(block)){
            logger.debug("区块数据异常，未满足共识规则。");
            return false;
        }

        //从交易角度校验每一笔交易
        for(Transaction transaction : block.getTransactions()){
            boolean transactionCanAddToNextBlock = isTransactionCanAddToNextBlock(block,transaction);
            if(!transactionCanAddToNextBlock){
                logger.debug("区块数据异常，交易异常。");
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isTransactionCanAddToNextBlock(Block block, Transaction transaction) {
        //校验交易的结构
        if(!StructureSizeTool.isTransactionStructureLegal(transaction)){
            logger.debug("交易数据异常，请校验交易的结构。");
            return false;
        }
        //校验交易的存储容量
        if(!StructureSizeTool.isTransactionStorageCapacityLegal(transaction)){
            logger.debug("交易数据异常，请校验交易的大小。");
            return false;
        }

        //校验交易的属性是否与计算得来的一致
        if(!TransactionPropertyTool.isWritePropertiesRight(transaction)){
            return false;
        }


        //业务校验
        //校验交易金额
        if(!TransactionTool.isTransactionAmountLegal(transaction)){
            logger.debug("交易金额不合法");
            return false;
        }
        //校验是否双花
        if(isDoubleSpendAttackHappen(transaction)){
            logger.debug("交易数据异常，检测到双花攻击。");
            return false;
        }
        //新产生的哈希是否合法
        if(!isNewHashLegal(transaction)){
            logger.debug("区块数据异常，区块中新产生的哈希异常。");
            return false;
        }


        //根据交易类型，做进一步的校验
        if(transaction.getTransactionType() == TransactionType.COINBASE){
            //校验激励
            if(!isIncentiveRight(block,transaction)){
                logger.debug("区块数据异常，激励异常。");
                return false;
            }
            return true;
        } else if(transaction.getTransactionType() == TransactionType.NORMAL){
            //交易输入必须要大于交易输出
            if(!TransactionTool.isTransactionInputsGreatEqualThanOutputsRight(transaction)) {
                logger.debug("交易校验失败：交易输入必须要大于交易输出。");
                return false;
            }
            //转账手续费
            if(!TransactionTool.isTransactionFeeRight(transaction)) {
                logger.debug("交易校验失败：手续费不正确。");
                return false;
            }
            //脚本
            if(!TransactionTool.verifyScript(transaction)) {
                logger.debug("交易校验失败：交易脚本钥匙解锁交易脚本锁异常。");
                return false;
            }
            return true;
        } else {
            logger.debug("区块数据异常，不能识别的交易类型。");
            return false;
        }
    }
    //endregion



    //region 普通查询
    @Override
    public long queryBlockChainHeight() {
        byte[] bytesBlockChainHeight = LevelDBUtil.get(blockChainDB, BlockChainDataBaseKeyTool.buildBlockChainHeightKey());
        if(bytesBlockChainHeight == null){
            //区块链中没有区块，高度默认为0。
            return LongUtil.ZERO;
        }
        return LevelDBUtil.bytesToLong(bytesBlockChainHeight);
    }

    @Override
    public long queryTransactionSize() {
        byte[] byteTotalTransactionQuantity = LevelDBUtil.get(blockChainDB, BlockChainDataBaseKeyTool.buildTotalTransactionQuantityKey());
        if(byteTotalTransactionQuantity == null){
            return LongUtil.ZERO;
        }
        return LevelDBUtil.bytesToLong(byteTotalTransactionQuantity);
    }

    @Override
    public long queryBlockHeightByBlockHash(String blockHash) {
        byte[] bytesBlockHeight = LevelDBUtil.get(blockChainDB, BlockChainDataBaseKeyTool.buildBlockHashToBlockHeightKey(blockHash));
        if(bytesBlockHeight == null){
            return LongUtil.ZERO;
        }
        return LevelDBUtil.bytesToLong(bytesBlockHeight);
    }

    @Override
    public String queryTransactionHashBySpendTransactionOutputId(TransactionOutputId transactionOutputId) {
        byte[] bytesTransactionHash = LevelDBUtil.get(blockChainDB, BlockChainDataBaseKeyTool.buildSpendTransactionOutputIdToTransactionHashKey(transactionOutputId));
        if(bytesTransactionHash == null){
            return null;
        }
        return EncodeDecodeUtil.decodeToTransactionHash(bytesTransactionHash);
    }
    //endregion



    //region 区块查询
    @Override
    public Block queryTailBlock() {
        long blockChainHeight = queryBlockChainHeight();
        if(LongUtil.isLessEqualThan(blockChainHeight,LongUtil.ZERO)){
            return null;
        }
        return queryBlockByBlockHeight(blockChainHeight);
    }
    @Override
    public Block queryBlockByBlockHeight(long blockHeight) {
        byte[] bytesBlock = LevelDBUtil.get(blockChainDB, BlockChainDataBaseKeyTool.buildBlockHeightToBlockKey(blockHeight));
        if(bytesBlock==null){
            return null;
        }
        return EncodeDecodeUtil.decodeToBlock(bytesBlock);
    }
    @Override
    public Block queryBlockByBlockHash(String blockHash) {
        long blockHeight = queryBlockHeightByBlockHash(blockHash);
        if(LongUtil.isLessEqualThan(blockHeight,LongUtil.ZERO)){
            return null;
        }
        return queryBlockByBlockHeight(blockHeight);

    }
    //endregion



    //region 交易查询
    @Override
    public Transaction queryTransactionByTransactionHash(String transactionHash) {
        byte[] bytesTransaction = LevelDBUtil.get(blockChainDB, BlockChainDataBaseKeyTool.buildTransactionHashToTransactionKey(transactionHash));
        if(bytesTransaction==null){
            return null;
        }
        return EncodeDecodeUtil.decodeToTransaction(bytesTransaction);
    }

    @Override
    public List<Transaction> queryTransactionByTransactionHeight(long from,long size) {
        List<Transaction> transactionList = new ArrayList<>();
        for(long index=from; LongUtil.isLessThan(index,from+size); index++){
            byte[] byteTransaction = LevelDBUtil.get(blockChainDB, BlockChainDataBaseKeyTool.buildTransactionSequenceNumberInBlockChainToTransactionKey(index));
            if(byteTransaction == null){
                break;
            }
            Transaction transaction = EncodeDecodeUtil.decodeToTransaction(byteTransaction);
            transactionList.add(transaction);
        }
        return transactionList;
    }
    //endregion



    //region 交易输出查询
    public TransactionOutput queryTransactionOutputByTransactionOutputId(TransactionOutputId transactionOutputId) {
        byte[] bytesTransactionOutput = LevelDBUtil.get(blockChainDB, BlockChainDataBaseKeyTool.buildTransactionOutputIdToTransactionOutputKey(transactionOutputId));
        if(bytesTransactionOutput == null){
            return null;
        }
        return EncodeDecodeUtil.decodeToTransactionOutput(bytesTransactionOutput);
    }

    @Override
    public TransactionOutput queryUnspendTransactionOutputByTransactionOutputId(TransactionOutputId transactionOutputId) {
        byte[] bytesUtxo = LevelDBUtil.get(blockChainDB, BlockChainDataBaseKeyTool.buildUnspendTransactionOutputIdToUnspendTransactionOutputKey(transactionOutputId));
        if(bytesUtxo == null){
            return null;
        }
        return EncodeDecodeUtil.decodeToTransactionOutput(bytesUtxo);
    }

    @Override
    public List<TransactionOutput> queryTransactionOutputListByAddress(String address,long from,long size) {
        List<TransactionOutput> transactionOutputList = new ArrayList<>();
        DBIterator iterator = blockChainDB.iterator();
        byte[] addressToTransactionOutputListKey = BlockChainDataBaseKeyTool.buildAddressToTransactionOutputListKey(address);
        int currentFrom = 0;
        int currentSize = 0;
        for (iterator.seek(addressToTransactionOutputListKey); iterator.hasNext(); iterator.next()) {
            byte[] byteKey = iterator.peekNext().getKey();
            if(Bytes.indexOf(byteKey,addressToTransactionOutputListKey) != 0){
                break;
            }
            byte[] byteValue = iterator.peekNext().getValue();
            if(byteValue == null || byteValue.length==0){
                continue;
            }
            currentFrom++;
            if(currentFrom>=from && currentSize<size){
                TransactionOutput transactionOutput = EncodeDecodeUtil.decodeToTransactionOutput(byteValue);
                transactionOutputList.add(transactionOutput);
                currentSize++;
            }
            if(currentSize>=size){
                break;
            }
        }
        return transactionOutputList;
    }

    @Override
    public List<TransactionOutput> queryUnspendTransactionOutputListByAddress(String address,long from,long size) {
        List<TransactionOutput> transactionOutputList = new ArrayList<>();
        DBIterator iterator = blockChainDB.iterator();
        byte[] addressToUnspendTransactionOutputListKey = BlockChainDataBaseKeyTool.buildAddressToUnspendTransactionOutputListKey(address);
        int currentFrom = 0;
        int currentSize = 0;
        for (iterator.seek(addressToUnspendTransactionOutputListKey); iterator.hasNext(); iterator.next()) {
            byte[] byteKey = iterator.peekNext().getKey();
            if(Bytes.indexOf(byteKey,addressToUnspendTransactionOutputListKey) != 0){
                break;
            }
            byte[] byteValue = iterator.peekNext().getValue();
            if(byteValue == null || byteValue.length==0){
                continue;
            }
            currentFrom++;
            if(currentFrom>=from && currentSize<size){
                TransactionOutput transactionOutput = EncodeDecodeUtil.decodeToTransactionOutput(byteValue);
                transactionOutputList.add(transactionOutput);
                currentSize++;
            }
            if(currentSize>=size){
                break;
            }
        }
        return transactionOutputList;
    }
    //endregion



    //region 拼装WriteBatch
    /**
     * 根据区块信息组装WriteBatch对象
     */
    private WriteBatch createWriteBatch(Block block, BlockChainActionEnum blockChainActionEnum) {
        WriteBatch writeBatch = new WriteBatchImpl();
        fillWriteBatch(writeBatch,block,blockChainActionEnum);
        return writeBatch;
    }
    /**
     * 把区块信息组装进WriteBatch对象
     */
    private void fillWriteBatch(WriteBatch writeBatch, Block block, BlockChainActionEnum blockChainActionEnum) {
        fillBlockProperty(block);
        //存储区块链高度到区块的映射
        byte[] blockHeightKey = BlockChainDataBaseKeyTool.buildBlockHeightToBlockKey(block.getHeight());
        if(BlockChainActionEnum.ADD_BLOCK == blockChainActionEnum){
            writeBatch.put(blockHeightKey, EncodeDecodeUtil.encode(block));
        }else{
            writeBatch.delete(blockHeightKey);
        }
        //存储区块链中总的交易数量
        long transactionSize = queryTransactionSize();
        byte[] totalTransactionQuantityKey = BlockChainDataBaseKeyTool.buildTotalTransactionQuantityKey();
        if(BlockChainActionEnum.ADD_BLOCK == blockChainActionEnum){
            writeBatch.put(totalTransactionQuantityKey, LevelDBUtil.longToBytes(transactionSize + BlockTool.getTransactionCount(block)));
        }else{
            writeBatch.put(totalTransactionQuantityKey, LevelDBUtil.longToBytes(transactionSize - BlockTool.getTransactionCount(block)));
        }
        //存储区块Hash到区块高度的映射
        byte[] blockHashBlockHeightKey = BlockChainDataBaseKeyTool.buildBlockHashToBlockHeightKey(block.getHash());
        if(BlockChainActionEnum.ADD_BLOCK == blockChainActionEnum){
            writeBatch.put(blockHashBlockHeightKey, LevelDBUtil.longToBytes(block.getHeight()));
        }else{
            writeBatch.delete(blockHashBlockHeightKey);
        }
        //存储区块链的高度
        byte[] blockChainHeightKey = BlockChainDataBaseKeyTool.buildBlockChainHeightKey();
        if(BlockChainActionEnum.ADD_BLOCK == blockChainActionEnum){
            writeBatch.put(blockChainHeightKey,LevelDBUtil.longToBytes(block.getHeight()));
        }else{
            writeBatch.put(blockChainHeightKey,LevelDBUtil.longToBytes(block.getHeight()-1));
        }

        List<Transaction> transactionList = block.getTransactions();
        if(transactionList != null){
            for(Transaction transaction:transactionList){
                //更新交易数据
                byte[] transactionHashToTransactionKey = BlockChainDataBaseKeyTool.buildTransactionHashToTransactionKey(transaction.getTransactionHash());
                //更新区块链中的交易序列号数据
                byte[] transactionSequenceNumberInBlockChainToTransactionKey = BlockChainDataBaseKeyTool.buildTransactionSequenceNumberInBlockChainToTransactionKey(transaction.getTransactionSequenceNumberInBlockChain());
                if(BlockChainActionEnum.ADD_BLOCK == blockChainActionEnum){
                    writeBatch.put(transactionHashToTransactionKey, EncodeDecodeUtil.encode(transaction));
                    writeBatch.put(transactionSequenceNumberInBlockChainToTransactionKey, EncodeDecodeUtil.encode(transaction));
                } else {
                    writeBatch.delete(transactionHashToTransactionKey);
                    writeBatch.delete(transactionSequenceNumberInBlockChainToTransactionKey);
                }
                List<TransactionInput> inputs = transaction.getInputs();
                if(inputs != null){
                    for(TransactionInput transactionInput:inputs){
                        //存储未花费交易输出哈希到未花费交易输出的映射
                        TransactionOutput unspendTransactionOutput = transactionInput.getUnspendTransactionOutput();
                        byte[] unspendTransactionOutputIdToUnspendTransactionOutputKey = BlockChainDataBaseKeyTool.buildUnspendTransactionOutputIdToUnspendTransactionOutputKey(unspendTransactionOutput);
                        if(BlockChainActionEnum.ADD_BLOCK == blockChainActionEnum){
                            writeBatch.delete(unspendTransactionOutputIdToUnspendTransactionOutputKey);
                        } else {
                            writeBatch.put(unspendTransactionOutputIdToUnspendTransactionOutputKey, EncodeDecodeUtil.encode(unspendTransactionOutput));
                        }

                        byte[] spendTransactionOutputIdToTransactionHashKey = BlockChainDataBaseKeyTool.buildSpendTransactionOutputIdToTransactionHashKey(unspendTransactionOutput);
                        if(BlockChainActionEnum.ADD_BLOCK == blockChainActionEnum){
                            writeBatch.put(spendTransactionOutputIdToTransactionHashKey,EncodeDecodeUtil.encodeTransactionHash(transaction.getTransactionHash()));
                        } else {
                            writeBatch.delete(spendTransactionOutputIdToTransactionHashKey);
                        }
                    }
                }
                List<TransactionOutput> outputs = transaction.getOutputs();
                if(outputs != null){
                    for(TransactionOutput output:outputs){
                        //存储交易输出哈希到交易输出的映射
                        byte[] transactionOutputIdToTransactionOutputKey = BlockChainDataBaseKeyTool.buildTransactionOutputIdToTransactionOutputKey(output);
                        //存储未花费交易输出哈希到未花费交易输出的映射
                        byte[] unspendTransactionOutputIdToUnspendTransactionOutputKey = BlockChainDataBaseKeyTool.buildUnspendTransactionOutputIdToUnspendTransactionOutputKey(output);
                        if(BlockChainActionEnum.ADD_BLOCK == blockChainActionEnum){
                            writeBatch.put(transactionOutputIdToTransactionOutputKey, EncodeDecodeUtil.encode(output));
                            writeBatch.put(unspendTransactionOutputIdToUnspendTransactionOutputKey, EncodeDecodeUtil.encode(output));
                        } else {
                            writeBatch.delete(transactionOutputIdToTransactionOutputKey);
                            writeBatch.delete(unspendTransactionOutputIdToUnspendTransactionOutputKey);
                        }
                    }
                }
            }
        }
        addHashToWriteBatch(writeBatch,block,blockChainActionEnum);
        addAddressRelateInformationToWriteBatch(writeBatch,block,blockChainActionEnum);
    }
    /**
     * 存储已使用的哈希
     */
    private void addHashToWriteBatch(WriteBatch writeBatch, Block block, BlockChainActionEnum blockChainActionEnum) {
        byte[] blockHashKey = BlockChainDataBaseKeyTool.buildHashKey(block.getHash());
        if(BlockChainActionEnum.ADD_BLOCK == blockChainActionEnum){
            writeBatch.put(blockHashKey, blockHashKey);
        } else {
            writeBatch.delete(blockHashKey);
        }
        List<Transaction> transactionList = block.getTransactions();
        if(transactionList != null){
            for(Transaction transaction:transactionList){
                byte[] transactionHashKey = BlockChainDataBaseKeyTool.buildHashKey(transaction.getTransactionHash());
                if(BlockChainActionEnum.ADD_BLOCK == blockChainActionEnum){
                    writeBatch.put(transactionHashKey, transactionHashKey);
                } else {
                    writeBatch.delete(transactionHashKey);
                }
            }
        }
    }

    /**
     * 补充区块的属性
     */
    private void fillBlockProperty(Block block) {
        long transactionSequenceNumberInBlock = LongUtil.ZERO;
        long transactionSequenceNumberInBlockChain = queryTransactionSize();
        long blockHeight = block.getHeight();
        List<Transaction> transactions = block.getTransactions();
        long transactionQuantity = transactions==null?LongUtil.ZERO:transactions.size();
        block.setTransactionQuantity(transactionQuantity);
        block.setStartTransactionSequenceNumberInBlockChain(
                LongUtil.isEquals(transactionQuantity,LongUtil.ZERO)?
                        LongUtil.ZERO:
                        (transactionSequenceNumberInBlockChain+LongUtil.ONE));
        block.setEndTransactionSequenceNumberInBlockChain(transactionSequenceNumberInBlockChain+transactionQuantity);
        if(transactions != null){
            for(Transaction transaction:transactions){
                transactionSequenceNumberInBlock++;
                transactionSequenceNumberInBlockChain++;
                transaction.setBlockHeight(blockHeight);
                transaction.setTransactionSequenceNumberInBlock(transactionSequenceNumberInBlock);
                transaction.setTransactionSequenceNumberInBlockChain(transactionSequenceNumberInBlockChain);

                List<TransactionOutput> outputs = transaction.getOutputs();
                if(outputs != null){
                    for (int i=0; i <outputs.size(); i++){
                        TransactionOutput transactionOutput = outputs.get(i);
                        transactionOutput.setBlockHeight(blockHeight);
                        transactionOutput.setTransactionHash(transaction.getTransactionHash());
                        transactionOutput.setTransactionOutputSequence(i+LongUtil.ONE);
                        transactionOutput.setTransactionSequenceNumberInBlock(transaction.getTransactionSequenceNumberInBlock());
                    }
                }
            }
        }
    }
    /**
     * 添加地址为主键的信息到WriteBatch
     */
    private void addAddressRelateInformationToWriteBatch(WriteBatch writeBatch, Block block, BlockChainActionEnum blockChainActionEnum) {
        for(Transaction transaction : block.getTransactions()){
            if(transaction == null){
                return;
            }
            List<TransactionInput> inputs = transaction.getInputs();
            if(inputs != null){
                for (TransactionInput transactionInput:inputs){
                    TransactionOutput utxo = transactionInput.getUnspendTransactionOutput();
                    byte[] addressToUnspendTransactionOutputListKey = BlockChainDataBaseKeyTool.buildAddressToUnspendTransactionOutputListKey(utxo);
                    if(blockChainActionEnum == BlockChainActionEnum.ADD_BLOCK){
                        writeBatch.delete(addressToUnspendTransactionOutputListKey);
                    }else{
                        writeBatch.put(addressToUnspendTransactionOutputListKey, EncodeDecodeUtil.encode(utxo));
                    }
                }
            }

            List<TransactionOutput> outputs = transaction.getOutputs();
            if(outputs != null){
                for (TransactionOutput transactionOutput:outputs){
                    byte[] addressToTransactionOutputListKey = BlockChainDataBaseKeyTool.buildAddressToTransactionOutputListKey(transactionOutput);
                    byte[] addressToUnspendTransactionOutputListKey = BlockChainDataBaseKeyTool.buildAddressToUnspendTransactionOutputListKey(transactionOutput);
                    if(blockChainActionEnum == BlockChainActionEnum.ADD_BLOCK){
                        byte[] byteTransactionOutput = EncodeDecodeUtil.encode(transactionOutput);
                        writeBatch.put(addressToTransactionOutputListKey,byteTransactionOutput);
                        writeBatch.put(addressToUnspendTransactionOutputListKey,byteTransactionOutput);
                    }else{
                        writeBatch.delete(addressToTransactionOutputListKey);
                        writeBatch.delete(addressToUnspendTransactionOutputListKey);
                    }
                }
            }
        }
    }
    //endregion


    /**
     * 检查交易输入是否都是未花费交易输出
     */
    private boolean isTransactionInputFromUnspendTransactionOutput(Transaction transaction) {
        List<TransactionInput> inputs = transaction.getInputs();
        if(inputs != null){
            for(TransactionInput transactionInput : inputs) {
                TransactionOutput unspendTransactionOutput = transactionInput.getUnspendTransactionOutput();
                TransactionOutput transactionOutput = queryUnspendTransactionOutputByTransactionOutputId(unspendTransactionOutput);
                if(transactionOutput == null){
                    logger.debug("交易数据异常：交易输入不是未花费交易输出。");
                    return false;
                }
            }
        }
        return true;
    }


    //region 新产生的哈希相关
    /**
     * 哈希是否已经被区块链系统使用了？
     */
    private boolean isHashUsed(String hash){
        byte[] bytesHash = LevelDBUtil.get(blockChainDB, BlockChainDataBaseKeyTool.buildHashKey(hash));
        return bytesHash != null;
    }
    /**
     * 交易中新产生的哈希是否已经被区块链系统使用了？
     */
    private boolean isNewHashUsed(Transaction transaction) {
        //校验交易Hash是否已经被使用了
        String transactionHash = transaction.getTransactionHash();
        if(isHashUsed(transactionHash)){
            logger.debug("交易数据异常，交易Hash已经被使用了。");
            return false;
        }
        return true;
    }
    /**
     * 区块中新产生的哈希是否已经被区块链系统使用了？
     */
    private boolean isHashUsed(Block block) {
        //校验区块Hash是否已经被使用了
        String blockHash = block.getHash();
        if(isHashUsed(blockHash)){
            logger.debug("区块数据异常，区块Hash已经被使用了。");
            return false;
        }
        //校验每一笔交易新产生的Hash是否正确
        List<Transaction> blockTransactions = block.getTransactions();
        if(blockTransactions != null){
            for(Transaction transaction:blockTransactions){
                if(!isNewHashUsed(transaction)){
                    return false;
                }
            }
        }
        return true;
    }
    /**
     * 区块中新产生的哈希是否合法
     */
    private boolean isNewHashLegal(Transaction transaction) {
        //校验哈希作为主键的正确性
        //新产生的Hash不能被使用过
        if(!isNewHashUsed(transaction)){
            logger.debug("校验数据异常，校验中占用的部分主键已经被使用了。");
            return false;
        }
        return true;
    }
    /**
     * 区块中新产生的哈希是否合法
     */
    private boolean isNewHashLegal(Block block) {
        //校验哈希作为主键的正确性
        //新产生的哈希不能有重复
        if(!BlockTool.isExistDuplicateNewHash(block)){
            logger.debug("区块数据异常，区块中新产生的哈希有重复。");
            return false;
        }
        //新产生的哈希不能被区块链使用过了
        if(!isHashUsed(block)){
            logger.debug("区块数据异常，区块中新产生的哈希已经早被区块链使用了。");
            return false;
        }
        return true;
    }
    //endregion


    //region 双花攻击
    /**
     * 是否有双花攻击
     * 相关拓展：双花攻击 https://zhuanlan.zhihu.com/p/258952892
     */
    private boolean isDoubleSpendAttackHappen(Transaction transaction) {
        //双花交易：交易内部存在重复的(未花费交易输出)
        if(TransactionTool.isExistDuplicateTransactionInput(transaction)){
            logger.debug("交易数据异常，检测到双花攻击。");
            return true;
        }
        //双花交易：交易内部存在已经花费的(未花费交易输出)
        if(!isTransactionInputFromUnspendTransactionOutput(transaction)){
            logger.debug("交易数据异常：发生双花交易。");
            return true;
        }
        return false;
    }

    /**
     * 是否有双花攻击
     * 相关拓展：双花攻击 https://zhuanlan.zhihu.com/p/258952892
     */
    private boolean isDoubleSpendAttackHappen(Block block) {
        //双花交易：区块内部存在重复的(未花费交易输出)
        if(BlockTool.isExistDuplicateTransactionInput(block)){
            logger.debug("区块数据异常：发生双花交易。");
            return true;
        }
        //双花交易：区块内部存在已经花费的(未花费交易输出)
        for(Transaction transaction : block.getTransactions()){
            if(!isTransactionInputFromUnspendTransactionOutput(transaction)){
                logger.debug("区块数据异常：发生双花交易。");
                return true;
            }
        }
        return false;
    }
    //endregion

    /**
     * 激励交易正确吗？
     */
    private boolean isIncentiveRight(Block block, Transaction transaction) {
        //激励校验
        if(!TransactionTool.isIncentiveRight(incentive.mineAward(block),transaction)){
            logger.debug("区块数据异常，激励异常。");
            return false;
        }
        return true;
    }
    /**
     * 区块激励正确吗？
     */
    private boolean isIncentiveRight(Block block) {
        if(!BlockTool.isIncentiveRight(incentive.mineAward(block),block)){
            logger.debug("区块数据异常，激励异常。");
            return false;
        }
        return true;
    }
    /**
     * 区块满足共识规则吗？
     */
    private boolean isReachConsensus(Block block) {
        return consensus.isReachConsensus(this,block);
    }
}