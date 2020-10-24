package com.xingkaichun.helloworldblockchain.core;

import com.xingkaichun.helloworldblockchain.core.impl.*;
import com.xingkaichun.helloworldblockchain.core.tools.ResourcePathTool;

import java.io.File;

/**
 * 创建BlockChainCore的工厂
 *
 * @author 邢开春 微信HelloworldBlockchain 邮箱xingkaichun@qq.com
 */
public class BlockChainCoreFactory {

    /**
     * 创建BlockChainCore实例
     */
    public static BlockChainCore createBlockChainCore(){
        return createBlockChainCore(ResourcePathTool.getDataRootPath());
    }

    /**
     * 创建BlockChainCore实例
     *
     * @param blockchainDataPath 区块链数据存放位置
     */
    public static BlockChainCore createBlockChainCore(String blockchainDataPath) {

        Incentive incentive = new IncentiveDefaultImpl();
        Consensus consensus = new ProofOfWorkConsensusImpl();
        BlockChainDataBase blockChainDataBase = new BlockChainDataBaseDefaultImpl(blockchainDataPath,incentive,consensus);

        MinerTransactionDtoDataBase minerTransactionDtoDataBase = new MinerTransactionDtoDtoDataBaseDefaultImpl(blockchainDataPath);
        Wallet wallet = new WalletImpl(blockchainDataPath);
        Miner miner = new MinerDefaultImpl(wallet,blockChainDataBase,minerTransactionDtoDataBase);

        SynchronizerDataBase synchronizerDataBase = new SynchronizerDataBaseDefaultImpl(blockchainDataPath);
        BlockChainDataBase temporaryBlockChainDataBase = new BlockChainDataBaseDefaultImpl(new File(blockchainDataPath,"TemporaryBlockChainDataBase").getAbsolutePath(),incentive,consensus);
        Synchronizer synchronizer = new SynchronizerDefaultImpl(blockChainDataBase,temporaryBlockChainDataBase,synchronizerDataBase);

        BlockChainCore blockChainCore = new BlockChainCoreImpl(blockChainDataBase,wallet,miner,synchronizer);
        return blockChainCore;
    }
}
