package com.xingkaichun.helloworldblockchain.netcore;

import com.xingkaichun.helloworldblockchain.core.BlockChainCore;
import com.xingkaichun.helloworldblockchain.core.BlockChainCoreFactory;
import com.xingkaichun.helloworldblockchain.core.tools.ResourcePathTool;
import com.xingkaichun.helloworldblockchain.core.utils.FileUtil;
import com.xingkaichun.helloworldblockchain.netcore.dao.ConfigurationDao;
import com.xingkaichun.helloworldblockchain.netcore.dao.NodeDao;
import com.xingkaichun.helloworldblockchain.netcore.dao.impl.ConfigurationDaoImpl;
import com.xingkaichun.helloworldblockchain.netcore.dao.impl.NodeDaoImpl;
import com.xingkaichun.helloworldblockchain.netcore.netserver.BlockchainHttpServer;
import com.xingkaichun.helloworldblockchain.netcore.netserver.HttpServerHandlerResolver;
import com.xingkaichun.helloworldblockchain.netcore.service.*;

/**
 * 网络版区块链核心工厂
 *
 * @author 邢开春 微信HelloworldBlockchain 邮箱xingkaichun@qq.com
 */
public class NetBlockchainCoreFactory {

    /**
     * 创建NetBlockchainCore实例
     */
    public static NetBlockchainCore createNetBlockchainCore(){
        return createNetBlockchainCore(ResourcePathTool.getDataRootPath(),8444);
    }

    /**
     * 创建NetBlockchainCore实例
     *
     * @param dataRootPath 区块链数据存放位置
     * @param serverPort 区块链节点网络端口
     */
    public static NetBlockchainCore createNetBlockchainCore(String dataRootPath, int serverPort){
        if(dataRootPath == null){
            throw new NullPointerException("参数路径不能为空。");
        }
        FileUtil.mkdir(dataRootPath);


        ConfigurationDao configurationDao = new ConfigurationDaoImpl(dataRootPath);
        ConfigurationService configurationService = new ConfigurationServiceImpl(configurationDao);

        BlockChainCore blockChainCore = BlockChainCoreFactory.createBlockChainCore(dataRootPath);

        NodeDao nodeDao = new NodeDaoImpl(dataRootPath);

        NodeService nodeService = new NodeServiceImpl(nodeDao,configurationService);
        BlockchainNodeClientService blockchainNodeClientService = new BlockchainNodeClientServiceImpl(serverPort);

        SynchronizeRemoteNodeBlockService synchronizeRemoteNodeBlockService = new SynchronizeRemoteNodeBlockServiceImpl(blockChainCore,nodeService,blockchainNodeClientService,configurationService);

        HttpServerHandlerResolver httpServerHandlerResolver = new HttpServerHandlerResolver(blockChainCore,nodeService,configurationService);
        BlockchainHttpServer blockchainHttpServer = new BlockchainHttpServer(serverPort, httpServerHandlerResolver);
        NodeSearcher nodeSearcher = new NodeSearcher(configurationService,nodeService,blockchainNodeClientService);
        NodeBroadcaster nodeBroadcaster = new NodeBroadcaster(configurationService,nodeService,blockchainNodeClientService);
        BlockSearcher blockSearcher = new BlockSearcher(nodeService,synchronizeRemoteNodeBlockService,blockChainCore,configurationService,blockchainNodeClientService);
        BlockBroadcaster blockBroadcaster = new BlockBroadcaster(configurationService,nodeService,blockChainCore,blockchainNodeClientService);
        NetBlockchainCore netBlockchainCore
                = new NetBlockchainCore(blockChainCore, blockchainHttpServer, configurationService
                ,nodeSearcher,nodeBroadcaster,blockSearcher, blockBroadcaster
                ,nodeService,blockchainNodeClientService);
        return netBlockchainCore;
    }
}
