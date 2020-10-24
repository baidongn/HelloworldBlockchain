package com.xingkaichun.helloworldblockchain.core.tools;

import com.xingkaichun.helloworldblockchain.core.BlockChainDataBase;
import com.xingkaichun.helloworldblockchain.core.model.Block;
import com.xingkaichun.helloworldblockchain.core.model.script.ScriptKey;
import com.xingkaichun.helloworldblockchain.core.model.script.ScriptLock;
import com.xingkaichun.helloworldblockchain.core.model.transaction.*;
import com.xingkaichun.helloworldblockchain.core.script.StackBasedVirtualMachine;
import com.xingkaichun.helloworldblockchain.crypto.AccountUtil;
import com.xingkaichun.helloworldblockchain.netcore.transport.dto.*;
import com.xingkaichun.helloworldblockchain.setting.GlobalSetting;

import java.util.ArrayList;
import java.util.List;

/**
 * dto转model工具
 *
 * @author 邢开春 微信HelloworldBlockchain 邮箱xingkaichun@qq.com
 */
public class Dto2ModelTool {

    public static Block blockDto2Block(BlockChainDataBase blockChainDataBase, BlockDTO blockDTO) {
        //求上一个区块的hash
        String previousBlockHash = blockDTO.getPreviousBlockHash();
        Block previousBlock = blockChainDataBase.queryBlockByBlockHash(previousBlockHash);

        Block block = new Block();
        block.setTimestamp(blockDTO.getTimestamp());
        block.setPreviousBlockHash(previousBlockHash);
        block.setNonce(blockDTO.getNonce());
        block.setHash(BlockTool.calculateBlockHash(block));

        //简单校验hash的难度 构造能满足共识的hash很难
        if(blockChainDataBase.getConsensus().isReachConsensus(blockChainDataBase,block)){
            throw new RuntimeException();
        }

        long blockHeight = previousBlock==null? GlobalSetting.GenesisBlock.HEIGHT+1:previousBlock.getHeight()+1;
        List<Transaction> transactionList = transactionDto2Transaction(blockChainDataBase,blockDTO.getTransactionDtoList());
        String merkleTreeRoot = BlockTool.calculateBlockMerkleTreeRoot(block);

        block.setHeight(blockHeight);
        block.setTransactions(transactionList);
        block.setMerkleTreeRoot(merkleTreeRoot);
        return block;
    }

    private static List<Transaction> transactionDto2Transaction(BlockChainDataBase blockChainDataBase, List<TransactionDTO> transactionDtoList) {
        List<Transaction> transactionList = new ArrayList<>();
        if(transactionDtoList != null){
            for(TransactionDTO transactionDTO:transactionDtoList){
                Transaction transaction = transactionDto2Transaction(blockChainDataBase,transactionDTO);
                transactionList.add(transaction);
            }
        }
        return transactionList;
    }

    public static Transaction transactionDto2Transaction(BlockChainDataBase blockChainDataBase, TransactionDTO transactionDTO) {
        List<TransactionInput> inputs = new ArrayList<>();
        List<TransactionInputDTO> transactionInputDtoList = transactionDTO.getTransactionInputDtoList();
        if(transactionInputDtoList != null){
            for (TransactionInputDTO transactionInputDTO:transactionInputDtoList){
                UnspendTransactionOutputDTO unspendTransactionOutputDto = transactionInputDTO.getUnspendTransactionOutputDTO();
                TransactionOutputId transactionOutputId = new TransactionOutputId();
                transactionOutputId.setTransactionHash(unspendTransactionOutputDto.getTransactionHash());
                transactionOutputId.setTransactionOutputSequence(unspendTransactionOutputDto.getTransactionOutputIndex());
                TransactionOutput unspendTransactionOutput = blockChainDataBase.queryUnspendTransactionOutputByTransactionOutputId(transactionOutputId);
                if(unspendTransactionOutput == null){
                    throw new ClassCastException("UnspendTransactionOutput不应该是null。");
                }
                TransactionInput transactionInput = new TransactionInput();
                transactionInput.setUnspendTransactionOutput(TransactionTool.transactionOutput2UnspendTransactionOutput(unspendTransactionOutput));
                transactionInput.setScriptKey(scriptKeyDto2ScriptKey(transactionInputDTO.getScriptKeyDTO()));
                inputs.add(transactionInput);
            }
        }

        List<TransactionOutput> outputs = new ArrayList<>();
        List<TransactionOutputDTO> dtoOutputs = transactionDTO.getTransactionOutputDtoList();
        if(dtoOutputs != null){
            for(TransactionOutputDTO transactionOutputDTO:dtoOutputs){
                TransactionOutput transactionOutput = transactionOutputDto2TransactionOutput(transactionOutputDTO);
                outputs.add(transactionOutput);
            }
        }

        Transaction transaction = new Transaction();
        TransactionType transactionType = obtainTransactionDTO(transactionDTO);
        transaction.setTransactionType(transactionType);
        transaction.setTransactionHash(TransactionTool.calculateTransactionHash(transactionDTO));
        transaction.setInputs(inputs);
        transaction.setOutputs(outputs);
        return transaction;
    }

    public static TransactionOutput transactionOutputDto2TransactionOutput(TransactionOutputDTO transactionOutputDTO) {
        TransactionOutput transactionOutput = new TransactionOutput();
        String publicKeyHash = StackBasedVirtualMachine.getPublicKeyHashByPayToPublicKeyHashOutputScript(transactionOutputDTO.getScriptLockDTO());
        String address = AccountUtil.addressFromPublicKeyHash(publicKeyHash);
        transactionOutput.setAddress(address);
        transactionOutput.setValue(transactionOutputDTO.getValue());
        transactionOutput.setScriptLock(scriptLockDto2ScriptLock(transactionOutputDTO.getScriptLockDTO()));
        return transactionOutput;
    }

    private static TransactionType obtainTransactionDTO(TransactionDTO transactionDTO) {
        if(transactionDTO.getTransactionInputDtoList() == null || transactionDTO.getTransactionInputDtoList().size()==0){
            return TransactionType.COINBASE;
        }
        return TransactionType.NORMAL;
    }

    private static ScriptLock scriptLockDto2ScriptLock(ScriptLockDTO scriptLockDTO) {
        if(scriptLockDTO == null){
            return null;
        }
        ScriptLock scriptLock = new ScriptLock();
        scriptLock.addAll(scriptLockDTO);
        return scriptLock;
    }

    private static ScriptKey scriptKeyDto2ScriptKey(ScriptKeyDTO scriptKeyDTO) {
        if(scriptKeyDTO == null){
            return null;
        }
        ScriptKey scriptKey = new ScriptKey();
        scriptKey.addAll(scriptKeyDTO);
        return scriptKey;
    }
}
