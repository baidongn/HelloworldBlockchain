package com.xingkaichun.helloworldblockchain.node.controller;

import com.google.common.base.Strings;
import com.xingkaichun.helloworldblockchain.core.BlockChainCore;
import com.xingkaichun.helloworldblockchain.core.model.Block;
import com.xingkaichun.helloworldblockchain.core.model.pay.Recipient;
import com.xingkaichun.helloworldblockchain.core.model.transaction.Transaction;
import com.xingkaichun.helloworldblockchain.core.model.transaction.TransactionInput;
import com.xingkaichun.helloworldblockchain.core.model.transaction.TransactionOutput;
import com.xingkaichun.helloworldblockchain.core.model.transaction.TransactionOutputId;
import com.xingkaichun.helloworldblockchain.core.tools.BlockTool;
import com.xingkaichun.helloworldblockchain.core.tools.ScriptTool;
import com.xingkaichun.helloworldblockchain.core.tools.TransactionTool;
import com.xingkaichun.helloworldblockchain.crypto.AccountUtil;
import com.xingkaichun.helloworldblockchain.crypto.model.Account;
import com.xingkaichun.helloworldblockchain.netcore.NetBlockchainCore;
import com.xingkaichun.helloworldblockchain.netcore.dto.common.ServiceResult;
import com.xingkaichun.helloworldblockchain.netcore.dto.common.page.PageCondition;
import com.xingkaichun.helloworldblockchain.netcore.dto.netserver.NodeDto;
import com.xingkaichun.helloworldblockchain.netcore.dto.transaction.SubmitTransactionDto;
import com.xingkaichun.helloworldblockchain.netcore.dto.transaction.SubmitTransactionResultDto;
import com.xingkaichun.helloworldblockchain.netcore.transport.dto.TransactionDTO;
import com.xingkaichun.helloworldblockchain.node.dto.blockchainbrowser.BlockChainApiRoute;
import com.xingkaichun.helloworldblockchain.node.dto.blockchainbrowser.block.*;
import com.xingkaichun.helloworldblockchain.node.dto.blockchainbrowser.request.*;
import com.xingkaichun.helloworldblockchain.node.dto.blockchainbrowser.response.*;
import com.xingkaichun.helloworldblockchain.node.dto.blockchainbrowser.transaction.*;
import com.xingkaichun.helloworldblockchain.node.util.DateUtil;
import com.xingkaichun.helloworldblockchain.setting.GlobalSetting;
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
 * 区块链浏览器控制器
 *
 * @author 邢开春 微信HelloworldBlockchain 邮箱xingkaichun@qq.com
 */
@Controller
@RequestMapping
public class BlockChainBrowserController {

    private static final Logger logger = LoggerFactory.getLogger(BlockChainBrowserController.class);

    @Autowired
    private NetBlockchainCore netBlockchainCore;


   /**
     * 生成账户(公钥、私钥、地址)
     */
    @ResponseBody
    @RequestMapping(value = BlockChainApiRoute.GENERATE_ACCOUNT,method={RequestMethod.GET,RequestMethod.POST})
    public ServiceResult<GenerateAccountResponse> generateAccount(@RequestBody GenerateAccountRequest request){
        try {
            Account account = AccountUtil.randomAccount();
            GenerateAccountResponse response = new GenerateAccountResponse();
            response.setAccount(account);
            return ServiceResult.createSuccessServiceResult("生成账户成功",response);
        } catch (Exception e){
            String message = "生成账户失败";
            logger.error(message,e);
            return ServiceResult.createFailServiceResult(message);
        }
    }

    /**
     * 提交交易到区块链网络
     */
    @ResponseBody
    @RequestMapping(value = BlockChainApiRoute.SUBMIT_TRANSACTION,method={RequestMethod.GET,RequestMethod.POST})
    public ServiceResult<SubmitTransactionResponseDto> submitTransaction(@RequestBody SubmitTransactionRequest request){
        try {
            SubmitTransactionDto submitTransactionDto = request.getSubmitTransactionDto();
            List<String> privateKeyList = submitTransactionDto.getPrivateKeyList();
            if(privateKeyList == null || privateKeyList.isEmpty()){
                return ServiceResult.createFailServiceResult("私钥不能为空");
            }
            try {
                for(String privateKey:privateKeyList){
                    AccountUtil.accountFromPrivateKey(privateKey);
                }
            } catch (Exception e){
                return ServiceResult.createFailServiceResult("私钥不正确，请检查输入的私钥");
            }
            List<Recipient> recipientList = submitTransactionDto.getRecipientList();
            if(recipientList == null || recipientList.isEmpty()){
                return ServiceResult.createFailServiceResult("交易输出不能为空。");
            }
            for(Recipient recipient:recipientList){
                if(Strings.isNullOrEmpty(recipient.getAddress())){
                    return ServiceResult.createFailServiceResult("交易输出的地址不能为空。");
                }
            }
            SubmitTransactionResultDto submitTransactionResultDto = netBlockchainCore.submitTransaction(request.getSubmitTransactionDto());
            SubmitTransactionResponseDto response = new SubmitTransactionResponseDto();
            response.setTransactionHash(submitTransactionResultDto.getTransactionHash());
            response.setTransactionDTO(submitTransactionResultDto.getTransactionDTO());
            response.setSuccessSubmitNode(submitTransactionResultDto.getSuccessSubmitNode());
            response.setFailSubmitNode(submitTransactionResultDto.getFailSubmitNode());
            return ServiceResult.createSuccessServiceResult("提交交易到区块链网络成功",response);
        } catch (Exception e){
            String message = "提交交易到区块链网络失败";
            logger.error(message,e);
            return ServiceResult.createFailServiceResult(message);
        }
    }

    /**
     * 根据交易Hash查询交易
     */
    @ResponseBody
    @RequestMapping(value = BlockChainApiRoute.QUERY_TRANSACTION_BY_TRANSACTION_HASH,method={RequestMethod.GET,RequestMethod.POST})
    public ServiceResult<QueryTransactionByTransactionHashResponse> queryTransactionByTransactionHash(@RequestBody QueryTransactionByTransactionHashRequest request){
        try {
            Transaction transaction = getBlockChainCore().queryTransactionByTransactionHash(request.getTransactionHash());
            if(transaction == null){
                return ServiceResult.createFailServiceResult(String.format("区块链中不存在交易哈希[%s]，请检查输入的交易哈希。",request.getTransactionHash()));
            }
            long blockChainHeight = getBlockChainCore().queryBlockChainHeight();
            Block block = getBlockChainCore().queryBlockByBlockHeight(transaction.getBlockHeight());
            QueryTransactionByTransactionHashResponse.TransactionDto transactionDto = new QueryTransactionByTransactionHashResponse.TransactionDto();

            transactionDto.setTransactionHash(transaction.getTransactionHash());
            transactionDto.setBlockHeight(transaction.getBlockHeight());
            transactionDto.setConfirmCount(blockChainHeight-block.getHeight());
            transactionDto.setBlockTime(DateUtil.timestamp2ChinaTime(block.getTimestamp()));

            transactionDto.setTransactionFee(TransactionTool.calculateTransactionFee(transaction));
            transactionDto.setTransactionType(transaction.getTransactionType().name());
            transactionDto.setTransactionInputCount(TransactionTool.getTransactionInputCount(transaction));
            transactionDto.setTransactionOutputCount(TransactionTool.getTransactionOutputCount(transaction));
            transactionDto.setTransactionInputValues(TransactionTool.getInputsValue(transaction));
            transactionDto.setTransactionOutputValues(TransactionTool.getOutputsValue(transaction));

            List<TransactionInput> inputs = transaction.getInputs();
            List<QueryTransactionByTransactionHashResponse.TransactionInputDto> transactionInputDtoList = new ArrayList<>();
            if(inputs != null){
                for(TransactionInput transactionInput:inputs){
                    QueryTransactionByTransactionHashResponse.TransactionInputDto transactionInputDto = new QueryTransactionByTransactionHashResponse.TransactionInputDto();
                    transactionInputDto.setAddress(transactionInput.getUnspendTransactionOutput().getAddress());
                    transactionInputDto.setValue(transactionInput.getUnspendTransactionOutput().getValue());
                    transactionInputDto.setScriptKey(ScriptTool.toString(transactionInput.getScriptKey()));
                    transactionInputDto.setTransactionHash(transactionInput.getUnspendTransactionOutput().getTransactionHash());
                    transactionInputDto.setTransactionOutputSequence(transactionInput.getUnspendTransactionOutput().getTransactionOutputSequence());
                    transactionInputDtoList.add(transactionInputDto);
                }
            }

            List<TransactionOutput> outputs = transaction.getOutputs();
            List<QueryTransactionByTransactionHashResponse.TransactionOutputDto> transactionOutputDtoList = new ArrayList<>();
            if(outputs != null){
                for(TransactionOutput transactionOutput:outputs){
                    QueryTransactionByTransactionHashResponse.TransactionOutputDto transactionOutputDto = new QueryTransactionByTransactionHashResponse.TransactionOutputDto();
                    transactionOutputDto.setAddress(transactionOutput.getAddress());
                    transactionOutputDto.setValue(transactionOutput.getValue());
                    transactionOutputDto.setScriptLock(ScriptTool.toString(transactionOutput.getScriptLock()));
                    transactionOutputDto.setTransactionHash(transactionOutput.getTransactionHash());
                    transactionOutputDto.setTransactionOutputSequence(transactionOutput.getTransactionOutputSequence());
                    transactionOutputDtoList.add(transactionOutputDto);
                }
            }

            transactionDto.setTransactionInputDtoList(transactionInputDtoList);
            transactionDto.setTransactionOutputDtoList(transactionOutputDtoList);


            QueryTransactionByTransactionHashResponse response = new QueryTransactionByTransactionHashResponse();
            response.setTransactionDto(transactionDto);
            return ServiceResult.createSuccessServiceResult("根据交易哈希查询交易成功",response);
        } catch (Exception e){
            String message = "根据交易哈希查询交易失败";
            logger.error(message,e);
            return ServiceResult.createFailServiceResult(message);
        }
    }

    /**
     * 根据交易高度查询交易
     */
    @ResponseBody
    @RequestMapping(value = BlockChainApiRoute.QUERY_TRANSACTION_BY_TRANSACTION_HEIGHT,method={RequestMethod.GET,RequestMethod.POST})
    public ServiceResult<QueryTransactionByTransactionHeightResponse> queryTransactionByTransactionHeight(@RequestBody QueryTransactionByTransactionHeightRequest request){
        try {
            PageCondition pageCondition = request.getPageCondition();
            long from = pageCondition.getFrom() == null ? 1L : pageCondition.getFrom();
            long size = pageCondition.getSize() == null ? 10L : pageCondition.getSize();
            List<Transaction> transactionList = getBlockChainCore().queryTransactionByTransactionHeight(from,size);
            if(transactionList == null){
                return ServiceResult.createFailServiceResult(String.format("区块链中不存在交易高度[%s]，请检查输入的交易哈希。",request.getPageCondition().getFrom()));
            }
            QueryTransactionByTransactionHeightResponse response = new QueryTransactionByTransactionHeightResponse();
            response.setTransactionList(transactionList);
            return ServiceResult.createSuccessServiceResult("根据交易高度查询交易成功",response);
        } catch (Exception e){
            String message = "根据交易高度查询交易失败";
            logger.error(message,e);
            return ServiceResult.createFailServiceResult(message);
        }
    }

    /**
     * 根据交易哈希查询挖矿中交易
     */
    @ResponseBody
    @RequestMapping(value = BlockChainApiRoute.QUERY_MINING_TRANSACTION_BY_TRANSACTION_HASH,method={RequestMethod.GET,RequestMethod.POST})
    public ServiceResult<QueryMiningTransactionByTransactionHashResponse> queryMiningTransactionByTransactionHash(@RequestBody QueryMiningTransactionByTransactionHashRequest request){
        try {
            TransactionDTO transactionDTO = getBlockChainCore().queryMiningTransactionDtoByTransactionHash(request.getTransactionHash());
            if(transactionDTO == null){
                return ServiceResult.createFailServiceResult(String.format("交易哈希[%s]不是正在被挖矿的交易。",request.getTransactionHash()));
            }

            QueryMiningTransactionByTransactionHashResponse response = new QueryMiningTransactionByTransactionHashResponse();
            response.setTransactionDTO(transactionDTO);
            return ServiceResult.createSuccessServiceResult("根据交易哈希查询挖矿中交易成功",response);
        } catch (Exception e){
            String message = "根据交易哈希查询挖矿中交易失败";
            logger.error(message,e);
            return ServiceResult.createFailServiceResult(message);
        }
    }

    /**
     * 根据地址获取未花费交易输出
     */
    @ResponseBody
    @RequestMapping(value = BlockChainApiRoute.QUERY_UTXOS_BY_ADDRESS,method={RequestMethod.GET,RequestMethod.POST})
    public ServiceResult<QueryUtxosByAddressResponse> queryUtxosByAddress(@RequestBody QueryUtxosByAddressRequest request){
        try {
            PageCondition pageCondition = request.getPageCondition();
            long from = pageCondition.getFrom() == null ? 0L : pageCondition.getFrom();
            long size = pageCondition.getSize() == null ? 10L : pageCondition.getSize();
            List<TransactionOutput> utxoList = getBlockChainCore().queryUtxoListByAddress(request.getAddress(),from,size);
            List<QueryUtxosByAddressResponse.TransactionOutputDto> utxoDtoList = new ArrayList<>();
            if(utxoList != null){
                for(TransactionOutput transactionOutput:utxoList){
                    QueryUtxosByAddressResponse.TransactionOutputDto transactionOutputDto = new QueryUtxosByAddressResponse.TransactionOutputDto();
                    transactionOutputDto.setBlockHeight(transactionOutput.getBlockHeight());
                    transactionOutputDto.setTransactionHash(transactionOutput.getTransactionHash());
                    transactionOutputDto.setValue(transactionOutput.getValue());
                    transactionOutputDto.setScriptLock(ScriptTool.toString(transactionOutput.getScriptLock()));
                    utxoDtoList.add(transactionOutputDto);
                }
            }

            QueryUtxosByAddressResponse response = new QueryUtxosByAddressResponse();
            response.setUtxos(utxoDtoList);
            return ServiceResult.createSuccessServiceResult("根据地址获取未花费交易输出成功",response);
        } catch (Exception e){
            String message = "根据地址获取未花费交易输出失败";
            logger.error(message,e);
            return ServiceResult.createFailServiceResult(message);
        }
    }
    /**
     * 根据地址获取交易输出
     */
    @ResponseBody
    @RequestMapping(value = BlockChainApiRoute.QUERY_TXOS_BY_ADDRESS,method={RequestMethod.GET,RequestMethod.POST})
    public ServiceResult<QueryTxosByAddressResponse> queryTxosByAddress(@RequestBody QueryTxosByAddressRequest request){
        try {
            PageCondition pageCondition = request.getPageCondition();
            long from = pageCondition.getFrom() == null ? 0L : pageCondition.getFrom();
            long size = pageCondition.getSize() == null ? 10L : pageCondition.getSize();
            List<TransactionOutput> txoList = getBlockChainCore().queryTxoListByAddress(request.getAddress(),from,size);
            if(txoList == null){
                return ServiceResult.createFailServiceResult(String.format("地址[%s]没有对应的交易输出列表。",request.getAddress()));
            }
            List<QueryTxosByAddressResponse.TransactionOutputDto> txoDtoList = new ArrayList<>();
            if(txoList != null){
                for(TransactionOutput transactionOutput:txoList){
                    QueryTxosByAddressResponse.TransactionOutputDto transactionOutputDto = new QueryTxosByAddressResponse.TransactionOutputDto();
                    transactionOutputDto.setBlockHeight(transactionOutput.getBlockHeight());
                    transactionOutputDto.setTransactionHash(transactionOutput.getTransactionHash());
                    transactionOutputDto.setValue(transactionOutput.getValue());
                    transactionOutputDto.setScriptLock(ScriptTool.toString(transactionOutput.getScriptLock()));

                    TransactionOutputId transactionOutputId = new TransactionOutputId();
                    transactionOutputId.setTransactionHash(transactionOutput.getTransactionHash());
                    transactionOutputId.setTransactionOutputSequence(transactionOutput.getTransactionOutputSequence());
                    TransactionOutput transactionOutputTemp = getBlockChainCore().getBlockChainDataBase().queryUnspendTransactionOutputByTransactionOutputId(transactionOutputId);
                    transactionOutputDto.setSpend(transactionOutputTemp==null);
                    if(transactionOutputTemp==null){
                        String destinationTransactionHash = getBlockChainCore().getBlockChainDataBase().queryTransactionHashBySpendTransactionOutputId(transactionOutputId);
                        transactionOutputDto.setDestinationTransactionHash(destinationTransactionHash);
                    }
                    txoDtoList.add(transactionOutputDto);
                }
            }
            QueryTxosByAddressResponse response = new QueryTxosByAddressResponse();
            response.setTxos(txoDtoList);
            return ServiceResult.createSuccessServiceResult("[根据地址获取交易输出]成功",response);
        } catch (Exception e){
            String message = "[根据地址获取交易输出]失败";
            logger.error(message,e);
            return ServiceResult.createFailServiceResult(message);
        }
    }
    /**
     * 根据交易输出ID获取交易输出
     */
    @ResponseBody
    @RequestMapping(value = BlockChainApiRoute.QUERY_TXO_BY_TRANSACTION_OUTPUT_ID,method={RequestMethod.GET,RequestMethod.POST})
    public ServiceResult<QueryTxoByTransactionOutputIdResponse> queryTxoByTransactionOutputId(@RequestBody QueryTxoByTransactionOutputIdRequest request){
        try {
            TransactionOutputId transactionOutputId = request.getTransactionOutputId();
            TransactionOutput transactionOutput = getBlockChainCore().getBlockChainDataBase().queryTransactionOutputByTransactionOutputId(transactionOutputId);
            if(transactionOutput == null){
                return ServiceResult.createFailServiceResult("没有查询到交易输出。");
            }
            QueryTxoByTransactionOutputIdResponse.TransactionOutputDto transactionOutputDto = new QueryTxoByTransactionOutputIdResponse.TransactionOutputDto();
            transactionOutputDto.setBlockHeight(transactionOutput.getBlockHeight());
            transactionOutputDto.setTransactionHash(transactionOutput.getTransactionHash());
            transactionOutputDto.setValue(transactionOutput.getValue());
            transactionOutputDto.setScriptLock(ScriptTool.toString(transactionOutput.getScriptLock()));
            transactionOutputDto.setTransactionOutputIndex(transactionOutput.getTransactionOutputSequence());
            transactionOutputId.setTransactionHash(transactionOutput.getTransactionHash());
            transactionOutputId.setTransactionOutputSequence(transactionOutput.getTransactionOutputSequence());
            TransactionOutput transactionOutputTemp = getBlockChainCore().getBlockChainDataBase().queryUnspendTransactionOutputByTransactionOutputId(transactionOutputId);
            transactionOutputDto.setSpend(transactionOutputTemp==null);
            if(transactionOutputTemp==null){
                String transactionHash = getBlockChainCore().getBlockChainDataBase().queryTransactionHashBySpendTransactionOutputId(transactionOutputId);
                transactionOutputDto.setDestinationTransactionHash(transactionHash);
                //transactionOutputDto.setScriptKey(ScriptTool.toString(transactionInput.getScriptKey()));
            }

            QueryTxoByTransactionOutputIdResponse response = new QueryTxoByTransactionOutputIdResponse();
            response.setTransactionOutputDto(transactionOutputDto);
            return ServiceResult.createSuccessServiceResult("[查询交易输出]成功",response);
        } catch (Exception e){
            String message = "[查询交易输出]失败";
            logger.error(message,e);
            return ServiceResult.createFailServiceResult(message);
        }
    }
    /**
     * Ping节点
     */
    @ResponseBody
    @RequestMapping(value = BlockChainApiRoute.PING,method={RequestMethod.GET,RequestMethod.POST})
    public ServiceResult<PingResponse> ping(@RequestBody PingRequest request){
        try {
            List<NodeDto> nodeList = netBlockchainCore.getNodeService().queryAllNoForkNodeList();
            long blockChainHeight = getBlockChainCore().queryBlockChainHeight();
            PingResponse response = new PingResponse();
            response.setNodeList(nodeList);
            response.setBlockChainHeight(blockChainHeight);
            response.setBlockChainId(GlobalSetting.BLOCK_CHAIN_ID);
            response.setBlockChainVersion(GlobalSetting.SystemVersionConstant.obtainVersion());
            return ServiceResult.createSuccessServiceResult("查询节点信息成功",response);
        } catch (Exception e){
            String message = "查询节点信息失败";
            logger.error(message,e);
            return ServiceResult.createSuccessServiceResult(message,null);
        }
    }

    /**
     * 查询挖矿中的交易
     */
    @ResponseBody
    @RequestMapping(value = BlockChainApiRoute.QUERY_MINING_TRANSACTION_LIST,method={RequestMethod.GET,RequestMethod.POST})
    public ServiceResult<QueryMiningTransactionListResponse> queryMiningTransactionList(@RequestBody QueryMiningTransactionListRequest request){
        try {
            PageCondition pageCondition = request.getPageCondition();
            long from = pageCondition.getFrom() == null ? 0L : pageCondition.getFrom();
            long size = pageCondition.getSize() == null ? 10L : pageCondition.getSize();
            List<TransactionDTO> transactionDtoList = getBlockChainCore().queryMiningTransactionList(from,size);
            QueryMiningTransactionListResponse response = new QueryMiningTransactionListResponse();
            response.setTransactionDtoList(transactionDtoList);
            return ServiceResult.createSuccessServiceResult("查询挖矿中的交易成功",response);
        } catch (Exception e){
            String message = "查询挖矿中的交易失败";
            logger.error(message,e);
            return ServiceResult.createSuccessServiceResult(message,null);
        }
    }

    /**
     * 根据区块高度查询区块
     */
    @ResponseBody
    @RequestMapping(value = BlockChainApiRoute.QUERY_BLOCKDTO_BY_BLOCK_HEIGHT,method={RequestMethod.GET,RequestMethod.POST})
    public ServiceResult<QueryBlockDtoByBlockHeightResponse> queryBlockDtoByBlockHeight(@RequestBody QueryBlockDtoByBlockHeightRequest request){
        try {
            Block block = getBlockChainCore().queryBlockByBlockHeight(request.getBlockHeight());
            if(block == null){
                return ServiceResult.createFailServiceResult(String.format("区块链中不存在区块高度[%d]，请检查输入高度。",request.getBlockHeight()));
            }
            QueryBlockDtoByBlockHeightResponse response = new QueryBlockDtoByBlockHeightResponse();
            response.setBlock(block);
            return ServiceResult.createSuccessServiceResult("成功获取区块",response);
        } catch (Exception e){
            String message = "查询获取失败";
            logger.error(message,e);
            return ServiceResult.createFailServiceResult(message);
        }
    }

    /**
     * 根据区块哈希查询区块
     */
    @ResponseBody
    @RequestMapping(value = BlockChainApiRoute.QUERY_BLOCKDTO_BY_BLOCK_HASH,method={RequestMethod.GET,RequestMethod.POST})
    public ServiceResult<QueryBlockDtoByBlockHashResponse> queryBlockDtoByBlockHash(@RequestBody QueryBlockDtoByBlockHashRequest request){
        try {
            Block block = getBlockChainCore().queryBlockDtoByBlockHash(request.getBlockHash());
            if(block == null){
                return ServiceResult.createFailServiceResult(String.format("区块链中不存在区块哈希[%s]，请检查输入哈希。",request.getBlockHash()));
            }
            Block nextBlock = getBlockChainCore().queryBlockByBlockHeight(block.getHeight()+1);

            QueryBlockDtoByBlockHashResponse.BlockDto blockDto = new QueryBlockDtoByBlockHashResponse.BlockDto();
            blockDto.setHeight(block.getHeight());
            blockDto.setConfirmCount(BlockTool.getTransactionCount(block));
            blockDto.setBlockSize("12K");//TODO
            blockDto.setTransactionCount(BlockTool.getTransactionCount(block));
            blockDto.setTime(DateUtil.timestamp2ChinaTime(block.getTimestamp()));
            blockDto.setMinerIncentiveValue(BlockTool.getMinerIncentiveValue(block));

            blockDto.setMinerDifficulty(block.getBits());
            blockDto.setNonce(String.valueOf(block.getNonce()));
            blockDto.setHash(block.getHash());
            blockDto.setPreviousBlockHash(block.getPreviousBlockHash());
            blockDto.setNextBlockHash(nextBlock==null?null:nextBlock.getHash());
            blockDto.setMerkleTreeRoot(block.getMerkleTreeRoot());


            List<QueryBlockDtoByBlockHashResponse.TransactionDto> transactionDtoList = new ArrayList<>();
            List<Transaction> transactions = block.getTransactions();
            if(transactions != null){
                for(Transaction transaction:transactions){
                    QueryBlockDtoByBlockHashResponse.TransactionDto transactionDto = new QueryBlockDtoByBlockHashResponse.TransactionDto();
                    transactionDto.setTransactionFee(TransactionTool.calculateTransactionFee(transaction));
                    transactionDto.setTransactionHash(transaction.getTransactionHash());
                    transactionDto.setTime(DateUtil.timestamp2ChinaTime(block.getTimestamp()));
                    transactionDto.setTransactionType(transaction.getTransactionType().name());
                    transactionDto.setTransactionInputValues(TransactionTool.getInputsValue(transaction));
                    transactionDto.setTransactionOutputValues(TransactionTool.getOutputsValue(transaction));

                    List<TransactionInput> inputs = transaction.getInputs();
                    List<QueryBlockDtoByBlockHashResponse.TransactionInputDto> transactionInputDtoList = new ArrayList<>();
                    if(inputs != null){
                        for(TransactionInput transactionInput:inputs){
                            QueryBlockDtoByBlockHashResponse.TransactionInputDto transactionInputDto = new QueryBlockDtoByBlockHashResponse.TransactionInputDto();
                            transactionInputDto.setAddress(transactionInput.getUnspendTransactionOutput().getAddress());
                            transactionInputDto.setValue(transactionInput.getUnspendTransactionOutput().getValue());
                            transactionInputDtoList.add(transactionInputDto);
                        }
                    }

                    List<TransactionOutput> outputs = transaction.getOutputs();
                    List<QueryBlockDtoByBlockHashResponse.TransactionOutputDto> transactionOutputDtoList = new ArrayList<>();
                    if(outputs != null){
                        for(TransactionOutput transactionOutput:outputs){
                            QueryBlockDtoByBlockHashResponse.TransactionOutputDto transactionOutputDto = new QueryBlockDtoByBlockHashResponse.TransactionOutputDto();
                            transactionOutputDto.setAddress(transactionOutput.getAddress());
                            transactionOutputDto.setValue(transactionOutput.getValue());
                            transactionOutputDtoList.add(transactionOutputDto);
                        }
                    }

                    transactionDto.setTransactionInputDtoList(transactionInputDtoList);
                    transactionDto.setTransactionOutputDtoList(transactionOutputDtoList);
                    transactionDtoList.add(transactionDto);
                }
            }
            blockDto.setTransactionDtoList(transactionDtoList);


            QueryBlockDtoByBlockHashResponse response = new QueryBlockDtoByBlockHashResponse();
            response.setBlockDto(blockDto);
            return ServiceResult.createSuccessServiceResult("[根据区块哈希查询区块]成功",response);
        } catch (Exception e){
            String message = "[根据区块哈希查询区块]失败";
            logger.error(message,e);
            return ServiceResult.createFailServiceResult(message);
        }
    }

    /**
     * 查询最近的10个区块
     */
    @ResponseBody
    @RequestMapping(value = BlockChainApiRoute.QUERY_LAST10_BLOCKDTO,method={RequestMethod.GET,RequestMethod.POST})
    public ServiceResult<QueryLast10BlockDtoResponse> queryLast10BlockDto(@RequestBody QueryLast10BlockDtoRequest request){
        try {
            List<Block> blockList = new ArrayList<>();
            long blockChainHeight = getBlockChainCore().queryBlockChainHeight();
            long minBlockHeight = blockChainHeight-9>0?blockChainHeight-9:1;
            while (blockChainHeight >= minBlockHeight){
                Block block = getBlockChainCore().queryBlockByBlockHeight(blockChainHeight);
                blockList.add(block);
                blockChainHeight--;
            }

            QueryLast10BlockDtoResponse response = new QueryLast10BlockDtoResponse();
            List<QueryLast10BlockDtoResponse.BlockDto> blockDtoList = new ArrayList<>();
            for(Block block : blockList){
                QueryLast10BlockDtoResponse.BlockDto blockDto = new QueryLast10BlockDtoResponse.BlockDto();
                blockDto.setHeight(block.getHeight());
                blockDto.setBlockSize("12K");//TODO
                blockDto.setTransactionCount(BlockTool.getTransactionCount(block));
                blockDto.setMinerIncentiveValue(BlockTool.getMinerIncentiveValue(block));
                blockDto.setTime(DateUtil.timestamp2ChinaTime(block.getTimestamp()));
                blockDto.setHash(block.getHash());
                blockDtoList.add(blockDto);
            }
            response.setBlockDtoList(blockDtoList);
            return ServiceResult.createSuccessServiceResult("[查询最近的10个区块]成功",response);
        } catch (Exception e){
            String message = "[查询最近的10个区块]失败";
            logger.error(message,e);
            return ServiceResult.createFailServiceResult(message);
        }
    }
    private BlockChainCore getBlockChainCore(){
        return netBlockchainCore.getBlockChainCore();
    }
}