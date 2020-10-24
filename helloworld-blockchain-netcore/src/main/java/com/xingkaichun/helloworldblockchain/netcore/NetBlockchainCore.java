package com.xingkaichun.helloworldblockchain.netcore;

import com.xingkaichun.helloworldblockchain.core.BlockChainCore;
import com.xingkaichun.helloworldblockchain.core.model.pay.Recipient;
import com.xingkaichun.helloworldblockchain.core.tools.TransactionTool;
import com.xingkaichun.helloworldblockchain.netcore.dto.common.EmptyResponse;
import com.xingkaichun.helloworldblockchain.netcore.dto.common.ServiceResult;
import com.xingkaichun.helloworldblockchain.netcore.dto.configuration.ConfigurationDto;
import com.xingkaichun.helloworldblockchain.netcore.dto.configuration.ConfigurationEnum;
import com.xingkaichun.helloworldblockchain.netcore.dto.netserver.NodeDto;
import com.xingkaichun.helloworldblockchain.netcore.dto.transaction.NormalTransactionDto;
import com.xingkaichun.helloworldblockchain.netcore.dto.transaction.SubmitNormalTransactionResultDto;
import com.xingkaichun.helloworldblockchain.netcore.netserver.BlockchainHttpServer;
import com.xingkaichun.helloworldblockchain.netcore.service.BlockchainNodeClientService;
import com.xingkaichun.helloworldblockchain.netcore.service.ConfigurationService;
import com.xingkaichun.helloworldblockchain.netcore.service.NodeService;
import com.xingkaichun.helloworldblockchain.netcore.transport.dto.TransactionDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * 网络版区块链核心，代表一个完整的网络版区块链核心系统。
 * 网络版区块链核心系统，由以下几部分组成：
 * 1.单机版[没有网络交互版本]区块链核心
 * @see com.xingkaichun.helloworldblockchain.core.BlockChainCore
 * 2.节点搜寻器
 * @see com.xingkaichun.helloworldblockchain.netcore.NodeSearcher
 * 3.节点广播者
 * @see com.xingkaichun.helloworldblockchain.netcore.NodeBroadcaster
 * 4.区块搜寻器
 * @see com.xingkaichun.helloworldblockchain.netcore.BlockSearcher
 * 5.区块广播者
 * @see com.xingkaichun.helloworldblockchain.netcore.BlockBroadcaster
 *
 * @author 邢开春 微信HelloworldBlockchain 邮箱xingkaichun@qq.com
 */
public class NetBlockchainCore {

    private BlockChainCore blockChainCore;
    private BlockchainHttpServer blockchainHttpServer;
    private NodeSearcher nodeSearcher;
    private NodeBroadcaster nodeBroadcaster;
    private BlockSearcher blockSearcher;
    private BlockBroadcaster blockBroadcaster;

    private ConfigurationService configurationService;
    private NodeService nodeService;
    private BlockchainNodeClientService blockchainNodeClientService;
    public NetBlockchainCore(BlockChainCore blockChainCore
            , BlockchainHttpServer blockchainHttpServer, ConfigurationService configurationService
            , NodeSearcher nodeSearcher, NodeBroadcaster nodeBroadcaster
            , BlockSearcher blockSearcher , BlockBroadcaster blockBroadcaster
            , NodeService nodeService, BlockchainNodeClientService blockchainNodeClientService) {

        this.blockChainCore = blockChainCore;
        this.blockchainHttpServer = blockchainHttpServer;
        this.configurationService = configurationService;
        this.nodeSearcher = nodeSearcher;
        this.nodeBroadcaster = nodeBroadcaster;
        this.blockSearcher = blockSearcher;
        this.blockBroadcaster = blockBroadcaster;

        this.nodeService = nodeService;
        this.blockchainNodeClientService = blockchainNodeClientService;
        restoreConfiguration();
    }

    /**
     * 恢复配置
     */
    private void restoreConfiguration() {
        //是否激活矿工
        ConfigurationDto isMinerActiveConfigurationDto = configurationService.getConfigurationByConfigurationKey(ConfigurationEnum.IS_MINER_ACTIVE.name());
        if(Boolean.valueOf(isMinerActiveConfigurationDto.getConfValue())){
            blockChainCore.getMiner().active();
        }else {
            blockChainCore.getMiner().deactive();
        }
        //是否激活同步者
        ConfigurationDto isSynchronizerActiveConfigurationDto = configurationService.getConfigurationByConfigurationKey(ConfigurationEnum.IS_SYNCHRONIZER_ACTIVE.name());
        if(Boolean.valueOf(isSynchronizerActiveConfigurationDto.getConfValue())){
            blockChainCore.getSynchronizer().active();
        }else {
            blockChainCore.getSynchronizer().deactive();
        }
    }




    public void start() {
        //启动本地的单机区块链
        blockChainCore.start();
        //启动区块链节点服务器
        blockchainHttpServer.start();

        //启动节点搜寻器
        nodeSearcher.start();
        //启动节点广播器
        nodeBroadcaster.start();
        //启动区块搜寻器
        blockSearcher.start();
        //启动区块广播者
        blockBroadcaster.start();
    }








    public SubmitNormalTransactionResultDto submitTransaction(NormalTransactionDto normalTransactionDto) {
        List<Recipient> recipientList = new ArrayList<>();
        List<NormalTransactionDto.Output> outputs = normalTransactionDto.getOutputs();
        if(outputs != null){
            for(NormalTransactionDto.Output output:outputs){
                Recipient recipient = new Recipient();
                recipient.setAddress(output.getAddress());
                recipient.setValue(output.getValue());
                recipientList.add(recipient);
            }
        }

        TransactionDTO transactionDTO = blockChainCore.buildTransactionDTO(recipientList);
        blockChainCore.submitTransaction(transactionDTO);
        List<NodeDto> nodes = nodeService.queryAllNoForkAliveNodeList();

        List<SubmitNormalTransactionResultDto.Node> successSubmitNode = new ArrayList<>();
        List<SubmitNormalTransactionResultDto.Node> failSubmitNode = new ArrayList<>();
        if(nodes != null){
            for(NodeDto node:nodes){
                ServiceResult<EmptyResponse> submitSuccess = blockchainNodeClientService.sumiteTransaction(node,transactionDTO);
                if(ServiceResult.isSuccess(submitSuccess)){
                    successSubmitNode.add(new SubmitNormalTransactionResultDto.Node(node.getIp(),node.getPort()));
                } else {
                    failSubmitNode.add(new SubmitNormalTransactionResultDto.Node(node.getIp(),node.getPort()));
                }
            }
        }

        SubmitNormalTransactionResultDto response = new SubmitNormalTransactionResultDto();
        response.setTransactionDTO(transactionDTO);
        response.setSuccessSubmitNode(successSubmitNode);
        response.setFailSubmitNode(failSubmitNode);
        response.setTransactionHash(TransactionTool.calculateTransactionHash(transactionDTO));
        return response;
    }

    //region get set
    public BlockChainCore getBlockChainCore() {
        return blockChainCore;
    }

    public BlockchainHttpServer getBlockchainHttpServer() {
        return blockchainHttpServer;
    }

    public NodeSearcher getNodeSearcher() {
        return nodeSearcher;
    }

    public BlockSearcher getBlockSearcher() {
        return blockSearcher;
    }

    public BlockBroadcaster getBlockBroadcaster() {
        return blockBroadcaster;
    }

    public ConfigurationService getConfigurationService() {
        return configurationService;
    }

    public NodeBroadcaster getNodeBroadcaster() {
        return nodeBroadcaster;
    }

    public NodeService getNodeService() {
        return nodeService;
    }

    public BlockchainNodeClientService getBlockchainNodeClientService() {
        return blockchainNodeClientService;
    }
    //end
}
