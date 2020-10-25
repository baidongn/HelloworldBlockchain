package com.xingkaichun.helloworldblockchain.node.controller;

import com.google.common.base.Strings;
import com.xingkaichun.helloworldblockchain.core.BlockChainCore;
import com.xingkaichun.helloworldblockchain.core.tools.WalletTool;
import com.xingkaichun.helloworldblockchain.crypto.AccountUtil;
import com.xingkaichun.helloworldblockchain.crypto.model.Account;
import com.xingkaichun.helloworldblockchain.netcore.NetBlockchainCore;
import com.xingkaichun.helloworldblockchain.netcore.dto.common.ServiceResult;
import com.xingkaichun.helloworldblockchain.netcore.dto.configuration.ConfigurationDto;
import com.xingkaichun.helloworldblockchain.netcore.dto.configuration.ConfigurationEnum;
import com.xingkaichun.helloworldblockchain.netcore.dto.netserver.NodeDto;
import com.xingkaichun.helloworldblockchain.node.dto.adminconsole.AdminConsoleApiRoute;
import com.xingkaichun.helloworldblockchain.node.dto.adminconsole.block.RemoveBlockRequest;
import com.xingkaichun.helloworldblockchain.node.dto.adminconsole.block.RemoveBlockResponse;
import com.xingkaichun.helloworldblockchain.node.dto.adminconsole.node.*;
import com.xingkaichun.helloworldblockchain.node.dto.adminconsole.account.AddAccountRequest;
import com.xingkaichun.helloworldblockchain.node.dto.adminconsole.account.DeleteAccountRequest;
import com.xingkaichun.helloworldblockchain.node.dto.adminconsole.account.QueryAllAccountListRequest;
import com.xingkaichun.helloworldblockchain.node.dto.adminconsole.miner.ActiveMinerRequest;
import com.xingkaichun.helloworldblockchain.node.dto.adminconsole.miner.DeactiveMinerRequest;
import com.xingkaichun.helloworldblockchain.node.dto.adminconsole.miner.IsMinerActiveRequest;
import com.xingkaichun.helloworldblockchain.node.dto.adminconsole.synchronizer.ActiveSynchronizerRequest;
import com.xingkaichun.helloworldblockchain.node.dto.adminconsole.synchronizer.DeactiveSynchronizerRequest;
import com.xingkaichun.helloworldblockchain.node.dto.adminconsole.synchronizer.IsSynchronizerActiveRequest;
import com.xingkaichun.helloworldblockchain.node.dto.adminconsole.account.AddAccountResponse;
import com.xingkaichun.helloworldblockchain.node.dto.adminconsole.account.DeleteAccountResponse;
import com.xingkaichun.helloworldblockchain.node.dto.adminconsole.account.QueryAllAccountListResponse;
import com.xingkaichun.helloworldblockchain.node.dto.adminconsole.miner.ActiveMinerResponse;
import com.xingkaichun.helloworldblockchain.node.dto.adminconsole.miner.DeactiveMinerResponse;
import com.xingkaichun.helloworldblockchain.node.dto.adminconsole.miner.IsMinerActiveResponse;
import com.xingkaichun.helloworldblockchain.node.dto.adminconsole.synchronizer.ActiveSynchronizerResponse;
import com.xingkaichun.helloworldblockchain.node.dto.adminconsole.synchronizer.DeactiveSynchronizerResponse;
import com.xingkaichun.helloworldblockchain.node.dto.adminconsole.synchronizer.IsSynchronizerActiveResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

/**
 * 管理员控制台的控制器：用于控制本地区块链节点，如激活矿工、停用矿工、同步其它节点数据等。
 *
 * @author 邢开春 微信HelloworldBlockchain 邮箱xingkaichun@qq.com
 */
@Controller
@RequestMapping
public class AdminConsoleController {

    private static final Logger logger = LoggerFactory.getLogger(AdminConsoleController.class);

    @Autowired
    private NetBlockchainCore netBlockchainCore;

    /**
     * 矿工是否激活
     */
    @ResponseBody
    @RequestMapping(value = AdminConsoleApiRoute.IS_MINER_ACTIVE,method={RequestMethod.GET,RequestMethod.POST})
    public ServiceResult<IsMinerActiveResponse> isMineActive(@RequestBody IsMinerActiveRequest request){
        try {
            boolean isMineActive = getBlockChainCore().getMiner().isActive();

            IsMinerActiveResponse response = new IsMinerActiveResponse();
            response.setMinerInActiveState(isMineActive);
            return ServiceResult.createSuccessServiceResult("查询矿工是否处于激活状态成功",response);
        } catch (Exception e){
            String message = "查询矿工是否处于激活状态失败";
            logger.error(message,e);
            return ServiceResult.createFailServiceResult(message);
        }
    }
    /**
     * 激活矿工
     */
    @ResponseBody
    @RequestMapping(value = AdminConsoleApiRoute.ACTIVE_MINER,method={RequestMethod.GET,RequestMethod.POST})
    public ServiceResult<ActiveMinerResponse> activeMiner(@RequestBody ActiveMinerRequest request){
        try {
            getBlockChainCore().getMiner().active();

            ConfigurationDto configurationDto = new ConfigurationDto(ConfigurationEnum.IS_MINER_ACTIVE.name(),String.valueOf(true));
            netBlockchainCore.getConfigurationService().setConfiguration(configurationDto);
            ActiveMinerResponse response = new ActiveMinerResponse();

            response.setActiveMinerSuccess(true);
            return ServiceResult.createSuccessServiceResult("激活矿工成功",response);
        } catch (Exception e){
            String message = "激活矿工失败";
            logger.error(message,e);
            return ServiceResult.createFailServiceResult(message);
        }
    }
    /**
     * 停用矿工
     */
    @ResponseBody
    @RequestMapping(value = AdminConsoleApiRoute.DEACTIVE_MINER,method={RequestMethod.GET,RequestMethod.POST})
    public ServiceResult<DeactiveMinerResponse> deactiveMiner(@RequestBody DeactiveMinerRequest request){
        try {
            getBlockChainCore().getMiner().deactive();

            ConfigurationDto configurationDto = new ConfigurationDto(ConfigurationEnum.IS_MINER_ACTIVE.name(),String.valueOf(false));
            netBlockchainCore.getConfigurationService().setConfiguration(configurationDto);
            DeactiveMinerResponse response = new DeactiveMinerResponse();

            response.setDeactiveMinerSuccess(true);
            return ServiceResult.createSuccessServiceResult("停用矿工成功",response);
        } catch (Exception e){
            String message = "停用矿工失败";
            logger.error(message,e);
            return ServiceResult.createFailServiceResult(message);
        }
    }



    /**
     * 同步器是否激活
     */
    @ResponseBody
    @RequestMapping(value = AdminConsoleApiRoute.IS_SYNCHRONIZER_ACTIVE,method={RequestMethod.GET,RequestMethod.POST})
    public ServiceResult<IsSynchronizerActiveResponse> isSynchronizerActive(@RequestBody IsSynchronizerActiveRequest request){
        try {
            boolean isSynchronizerActive = getBlockChainCore().getSynchronizer().isActive();

            IsSynchronizerActiveResponse response = new IsSynchronizerActiveResponse();
            response.setSynchronizerInActiveState(isSynchronizerActive);
            return ServiceResult.createSuccessServiceResult("查询同步器是否激活成功",response);
        } catch (Exception e){
            String message = "查询同步器是否激活失败";
            logger.error(message,e);
            return ServiceResult.createFailServiceResult(message);
        }
    }
    /**
     * 激活同步器
     */
    @ResponseBody
    @RequestMapping(value = AdminConsoleApiRoute.ACTIVE_SYNCHRONIZER,method={RequestMethod.GET,RequestMethod.POST})
    public ServiceResult<ActiveSynchronizerResponse> activeSynchronizer(@RequestBody ActiveSynchronizerRequest request){
        try {
            getBlockChainCore().getSynchronizer().active();

            ConfigurationDto configurationDto = new ConfigurationDto(ConfigurationEnum.IS_SYNCHRONIZER_ACTIVE.name(),String.valueOf(true));
            netBlockchainCore.getConfigurationService().setConfiguration(configurationDto);
            ActiveSynchronizerResponse response = new ActiveSynchronizerResponse();

            response.setActiveSynchronizerSuccess(true);
            return ServiceResult.createSuccessServiceResult("激活同步器成功",response);
        } catch (Exception e){
            String message = "激活同步器失败";
            logger.error(message,e);
            return ServiceResult.createFailServiceResult(message);
        }
    }
    /**
     * 停用同步器
     */
    @ResponseBody
    @RequestMapping(value = AdminConsoleApiRoute.DEACTIVE_SYNCHRONIZER,method={RequestMethod.GET,RequestMethod.POST})
    public ServiceResult<DeactiveSynchronizerResponse> deactiveSynchronizer(@RequestBody DeactiveSynchronizerRequest request){
        try {
            getBlockChainCore().getSynchronizer().deactive();

            ConfigurationDto configurationDto = new ConfigurationDto(ConfigurationEnum.IS_SYNCHRONIZER_ACTIVE.name(),String.valueOf(false));
            netBlockchainCore.getConfigurationService().setConfiguration(configurationDto);
            DeactiveSynchronizerResponse response = new DeactiveSynchronizerResponse();

            response.setDeactiveSynchronizerSuccess(true);
            return ServiceResult.createSuccessServiceResult("停用同步器成功",response);
        } catch (Exception e){
            String message = "停用同步器失败";
            logger.error(message,e);
            return ServiceResult.createFailServiceResult(message);
        }
    }


    /**
     * 新增节点
     */
    @ResponseBody
    @RequestMapping(value = AdminConsoleApiRoute.ADD_NODE,method={RequestMethod.GET,RequestMethod.POST})
    public ServiceResult<AddNodeResponse> addNode(@RequestBody AddNodeRequest request){
        try {
            NodeDto node = request.getNode();
            if(Strings.isNullOrEmpty(node.getIp())){
                return ServiceResult.createFailServiceResult("节点IP不能为空");
            }
            if(netBlockchainCore.getNodeService().queryNode(node) != null){
                return ServiceResult.createFailServiceResult("节点已经存在，不需要重复添加");
            }
            netBlockchainCore.getNodeService().addNode(node);
            AddNodeResponse response = new AddNodeResponse();
            response.setAddNodeSuccess(true);
            return ServiceResult.createSuccessServiceResult("新增节点成功",response);
        } catch (Exception e){
            String message = "新增节点失败";
            logger.error(message,e);
            return ServiceResult.createFailServiceResult(message);
        }
    }

    /**
     * 更新节点信息
     */
    @ResponseBody
    @RequestMapping(value = AdminConsoleApiRoute.UPDATE_NODE,method={RequestMethod.GET,RequestMethod.POST})
    public ServiceResult<UpdateNodeResponse> updateNode(@RequestBody UpdateNodeRequest request){
        try {
            if(request.getNode() == null){
                return ServiceResult.createFailServiceResult("请填写节点信息");
            }
            if(netBlockchainCore.getNodeService().queryNode(request.getNode()) == null){
                return ServiceResult.createFailServiceResult("节点不存在，无法更新");
            }
            netBlockchainCore.getNodeService().updateNode(request.getNode());
            UpdateNodeResponse response = new UpdateNodeResponse();
            return ServiceResult.createSuccessServiceResult("更新节点信息成功",response);
        } catch (Exception e){
            String message = "更新节点信息失败";
            logger.error(message,e);
            return ServiceResult.createFailServiceResult(message);
        }
    }

    /**
     * 删除节点
     */
    @ResponseBody
    @RequestMapping(value = AdminConsoleApiRoute.DELETE_NODE,method={RequestMethod.GET,RequestMethod.POST})
    public ServiceResult<DeleteNodeResponse> deleteNode(@RequestBody DeleteNodeRequest request){
        try {
            netBlockchainCore.getNodeService().deleteNode(request.getNode());
            DeleteNodeResponse response = new DeleteNodeResponse();
            return ServiceResult.createSuccessServiceResult("删除节点成功",response);
        } catch (Exception e){
            String message = "删除节点失败";
            logger.error(message,e);
            return ServiceResult.createFailServiceResult(message);
        }
    }

    /**
     * 查询节点
     */
    @ResponseBody
    @RequestMapping(value = AdminConsoleApiRoute.QUERY_ALL_NODE_LIST,method={RequestMethod.GET,RequestMethod.POST})
    public ServiceResult<QueryAllNodeListResponse> queryAllNodeList(@RequestBody QueryAllNodeListRequest request){
        try {
            List<NodeDto> nodeList = netBlockchainCore.getNodeService().queryAllNodeList();
            QueryAllNodeListResponse response = new QueryAllNodeListResponse();
            response.setNodeList(nodeList);
            return ServiceResult.createSuccessServiceResult("查询节点成功",response);
        } catch (Exception e){
            String message = "查询节点失败";
            logger.error(message,e);
            return ServiceResult.createFailServiceResult(message);
        }
    }

    /**
     * 查询是否允许自动搜索区块链节点
     */
    @ResponseBody
    @RequestMapping(value = AdminConsoleApiRoute.IS_AUTO_SEARCH_NODE,method={RequestMethod.GET,RequestMethod.POST})
    public ServiceResult<IsAutoSearchNodeResponse> isAutoSearchNewNode(@RequestBody IsAutoSearchNodeRequest request){
        try {
            ConfigurationDto configurationDto = netBlockchainCore.getConfigurationService().getConfigurationByConfigurationKey(ConfigurationEnum.AUTO_SEARCH_NODE.name());
            IsAutoSearchNodeResponse response = new IsAutoSearchNodeResponse();
            response.setAutoSearchNewNode(Boolean.valueOf(configurationDto.getConfValue()));
            return ServiceResult.createSuccessServiceResult("查询是否允许自动搜索区块链节点成功",response);
        } catch (Exception e){
            String message = "查询是否允许自动搜索区块链节点失败";
            logger.error(message,e);
            return ServiceResult.createFailServiceResult(message);
        }
    }

    /**
     * 设置是否允许自动搜索区块链节点
     */
    @ResponseBody
    @RequestMapping(value = AdminConsoleApiRoute.SET_AUTO_SEARCH_NODE,method={RequestMethod.GET,RequestMethod.POST})
    public ServiceResult<SetAutoSearchNodeResponse> setAutoSearchNode(@RequestBody SetAutoSearchNodeRequest request){
        try {
            ConfigurationDto configurationDto = new ConfigurationDto();
            configurationDto.setConfKey(ConfigurationEnum.AUTO_SEARCH_NODE.name());
            configurationDto.setConfValue(String.valueOf(request.isAutoSearchNode()));
            netBlockchainCore.getConfigurationService().setConfiguration(configurationDto);

            SetAutoSearchNodeResponse response = new SetAutoSearchNodeResponse();
            return ServiceResult.createSuccessServiceResult("设置是否允许自动搜索区块链节点成功",response);
        } catch (Exception e){
            String message = "设置是否允许自动搜索区块链节点失败";
            logger.error(message,e);
            return ServiceResult.createFailServiceResult(message);
        }
    }

    /**
     * 删除区块
     */
    @ResponseBody
    @RequestMapping(value = AdminConsoleApiRoute.REMOVE_BLOCK,method={RequestMethod.GET,RequestMethod.POST})
    public ServiceResult<RemoveBlockResponse> removeBlock(@RequestBody RemoveBlockRequest request){
        try {
            if(request.getBlockHeight() == null){
                return ServiceResult.createFailServiceResult("删除区块失败，区块高度不能空。");
            }
            getBlockChainCore().removeBlocksUtilBlockHeightLessThan(request.getBlockHeight());
            RemoveBlockResponse response = new RemoveBlockResponse();
            return ServiceResult.createSuccessServiceResult("删除区块成功",response);
        } catch (Exception e){
            String message = "删除区块失败";
            logger.error(message,e);
            return ServiceResult.createFailServiceResult(message);
        }
    }

    @ResponseBody
    @RequestMapping(value = AdminConsoleApiRoute.ADD_ACCOUNT,method={RequestMethod.GET,RequestMethod.POST})
    public ServiceResult<AddAccountResponse> addAccount(@RequestBody AddAccountRequest request){
        try {
            Account account = request.getAccount();
            if(account == null){
                return ServiceResult.createFailServiceResult("账户不能为空。");
            }
            String privateKey = account.getPrivateKey();
            if(Strings.isNullOrEmpty(privateKey)){
                return ServiceResult.createFailServiceResult("账户私钥不能为空。");
            }
            String publicKey = account.getPublicKey();
            if(!Strings.isNullOrEmpty(publicKey)){
                return ServiceResult.createFailServiceResult("账户公钥由私钥计算得出，不需要填写。");
            }
            String address = account.getAddress();
            if(!Strings.isNullOrEmpty(address)){
                return ServiceResult.createFailServiceResult("账户地址由私钥计算得出，不需要填写。");
            }

            Account accountTemp = AccountUtil.accountFromPrivateKey(privateKey);
            getBlockChainCore().getWallet().addAccount(accountTemp);
            AddAccountResponse response = new AddAccountResponse();
            response.setAddAccountSuccess(true);
            return ServiceResult.createSuccessServiceResult("删除区块成功",response);
        } catch (Exception e){
            String message = "新增账户失败";
            logger.error(message,e);
            return ServiceResult.createFailServiceResult(message);
        }
    }

    @ResponseBody
    @RequestMapping(value = AdminConsoleApiRoute.DELETE_ACCOUNT,method={RequestMethod.GET,RequestMethod.POST})
    public ServiceResult<DeleteAccountResponse> deleteAccount(@RequestBody DeleteAccountRequest request){
        try {
            DeleteAccountResponse response = new DeleteAccountResponse();
            response.setDeleteAccountSuccess(true);
            ServiceResult<DeleteAccountResponse> responseServiceResult = ServiceResult.createSuccessServiceResult("删除账号成功",response);

            Account account = request.getAccount();
            if(account == null){
                return ServiceResult.createFailServiceResult("账户不能为空。");
            }
            String address = account.getAddress();
            if(!Strings.isNullOrEmpty(address)){
                Account accountTemp = new Account(null,null,address);
                getBlockChainCore().getWallet().deleteAccount(accountTemp);
                return responseServiceResult;
            }
            String publicKey = account.getPublicKey();
            if(!Strings.isNullOrEmpty(publicKey)){
                Account accountTemp = new Account(null,null, AccountUtil.addressFromPublicKey(publicKey));
                getBlockChainCore().getWallet().deleteAccount(accountTemp);
                return responseServiceResult;
            }
            String privateKey = account.getPrivateKey();
            if(!Strings.isNullOrEmpty(privateKey)){
                Account accountTemp = new Account(null,null, AccountUtil.accountFromPrivateKey(privateKey).getAddress());
                getBlockChainCore().getWallet().deleteAccount(accountTemp);
                return responseServiceResult;
            }
            return ServiceResult.createFailServiceResult("请填写需要删除的地址");
        } catch (Exception e){
            String message = "删除账号失败";
            logger.error(message,e);
            return ServiceResult.createFailServiceResult(message);
        }
    }

    @ResponseBody
    @RequestMapping(value = AdminConsoleApiRoute.QUERY_ALL_ACCOUNTLIST,method={RequestMethod.GET,RequestMethod.POST})
    public ServiceResult<QueryAllAccountListResponse> queryAllAccountList(@RequestBody QueryAllAccountListRequest request){
        try {
            List<Account> allAccount = getBlockChainCore().getWallet().queryAllAccount();

            List<QueryAllAccountListResponse.AccountDto> accountDtoList = new ArrayList<>();
            if(allAccount != null){
                for(Account account:allAccount){
                    QueryAllAccountListResponse.AccountDto accountDto = new QueryAllAccountListResponse.AccountDto();
                    accountDto.setAddress(account.getAddress());
                    accountDto.setPrivateKey(account.getPrivateKey());
                    accountDto.setValue(WalletTool.obtainBalance(getBlockChainCore(),account.getAddress()));
                    accountDtoList.add(accountDto);
                }
            }
            long balance = 0;
            for(QueryAllAccountListResponse.AccountDto accountDto:accountDtoList){
                balance += accountDto.getValue();
            }
            QueryAllAccountListResponse response = new QueryAllAccountListResponse();
            response.setAccountDtoList(accountDtoList);
            response.setBalance(balance);
            return ServiceResult.createSuccessServiceResult("[查询所有账户]成功",response);
        } catch (Exception e){
            String message = "[查询所有账户]失败";
            logger.error(message,e);
            return ServiceResult.createFailServiceResult(message);
        }
    }

    private BlockChainCore getBlockChainCore(){
        return netBlockchainCore.getBlockChainCore();
    }
}