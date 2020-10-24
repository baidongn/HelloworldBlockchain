package com.xingkaichun.helloworldblockchain.core.impl;

import com.xingkaichun.helloworldblockchain.core.*;
import com.xingkaichun.helloworldblockchain.core.model.Block;
import com.xingkaichun.helloworldblockchain.core.model.pay.Recipient;
import com.xingkaichun.helloworldblockchain.core.model.script.ScriptKey;
import com.xingkaichun.helloworldblockchain.core.model.script.ScriptLock;
import com.xingkaichun.helloworldblockchain.core.model.transaction.Transaction;
import com.xingkaichun.helloworldblockchain.core.model.transaction.TransactionOutput;
import com.xingkaichun.helloworldblockchain.core.script.StackBasedVirtualMachine;
import com.xingkaichun.helloworldblockchain.core.tools.Dto2ModelTool;
import com.xingkaichun.helloworldblockchain.core.tools.Model2DtoTool;
import com.xingkaichun.helloworldblockchain.core.tools.TransactionTool;
import com.xingkaichun.helloworldblockchain.crypto.AccountUtil;
import com.xingkaichun.helloworldblockchain.crypto.model.Account;
import com.xingkaichun.helloworldblockchain.netcore.transport.dto.TransactionDTO;
import com.xingkaichun.helloworldblockchain.netcore.transport.dto.TransactionInputDTO;
import com.xingkaichun.helloworldblockchain.netcore.transport.dto.TransactionOutputDTO;
import com.xingkaichun.helloworldblockchain.netcore.transport.dto.UnspendTransactionOutputDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 默认实现
 * 
 * @author 邢开春 微信HelloworldBlockchain 邮箱xingkaichun@qq.com
 */
public class BlockChainCoreImpl extends BlockChainCore {

    private static final Logger logger = LoggerFactory.getLogger(BlockChainCoreImpl.class);

    public BlockChainCoreImpl(BlockChainDataBase blockChainDataBase, Wallet wallet, Miner miner, Synchronizer synchronizer) {
        super(blockChainDataBase,wallet,miner,synchronizer);
    }

    @Override
    public void start() {
        //启动区块链同步器线程
        new Thread(
                ()->{
                    try {
                        synchronizer.start();
                    } catch (Exception e) {
                        logger.error("区块链同步器在运行中发生异常并退出，请检查修复异常！",e);
                    }
                }
        ).start();
        //启动矿工线程
        new Thread(
                ()->{
                    try {
                        miner.start();
                    } catch (Exception e) {
                        logger.error("矿工在运行中发生异常并退出，请检查修复异常！",e);
                    }
                }
        ).start();
    }








    @Override
    public String queryBlockHashByBlockHeight(long blockHeight) {
        Block block = blockChainDataBase.queryBlockByBlockHeight(blockHeight);
        if(block == null){
            return null;
        }
        return block.getHash();
    }

    @Override
    public long queryBlockChainHeight() {
        return blockChainDataBase.queryBlockChainHeight();
    }







    @Override
    public Transaction queryTransactionByTransactionHash(String transactionHash) {
        Transaction transaction = blockChainDataBase.queryTransactionByTransactionHash(transactionHash);
        return transaction;
    }

    @Override
    public List<Transaction> queryTransactionByTransactionHeight(long from,long size) {
        List<Transaction>  transactionList = blockChainDataBase.queryTransactionByTransactionHeight(from,size);
        return transactionList;
    }

    @Override
    public List<TransactionOutput> queryUtxoListByAddress(String address,long from,long size) {
        List<TransactionOutput> utxo =  blockChainDataBase.queryUnspendTransactionOutputListByAddress(address,from,size);
        return utxo;
    }

    @Override
    public List<TransactionOutput> queryTxoListByAddress(String address,long from,long size) {
        List<TransactionOutput> utxo =  blockChainDataBase.queryTransactionOutputListByAddress(address,from,size);
        return utxo;
    }









    @Override
    public Block queryBlockByBlockHeight(long blockHeight) {
        return blockChainDataBase.queryBlockByBlockHeight(blockHeight);
    }

    @Override
    public Block queryBlockDtoByBlockHash(String blockHash) {
        return blockChainDataBase.queryBlockByBlockHash(blockHash);
    }



    @Override
    public void removeBlocksUtilBlockHeightLessThan(long blockHeight) {
        blockChainDataBase.removeBlocksUtilBlockHeightLessThan(blockHeight);
    }



    public TransactionDTO buildTransactionDTO(List<Recipient> recipientList) {
        List<Account> accountList = wallet.queryAllAccount();
        List<String> privateKeyList = new ArrayList<>();
        if(accountList != null){
            for(Account account:accountList){
                privateKeyList.add(account.getPrivateKey());
            }
        }
        return buildTransactionDTO(privateKeyList, recipientList);
    }

    private TransactionDTO buildTransactionDTO(List<String> privateKeyList,List<Recipient> recipientList) {
        //理应支付总金额
        long outputVaules = 0;
        if(recipientList != null){
            for(Recipient recipient : recipientList){
                outputVaules += recipient.getValue();
            }
        }
        //创建交易输出
        List<TransactionOutputDTO> transactionOutputDtoList = new ArrayList<>();
        if(recipientList != null){
            for(Recipient recipient : recipientList){
                TransactionOutputDTO transactionOutputDTO = new TransactionOutputDTO();
                transactionOutputDTO.setValue(recipient.getValue());
                ScriptLock scriptLock = StackBasedVirtualMachine.createPayToPublicKeyHashOutputScript(recipient.getAddress());
                transactionOutputDTO.setScriptLockDTO(Model2DtoTool.scriptLock2ScriptLockDTO(scriptLock));
                transactionOutputDtoList.add(transactionOutputDTO);
            }
        }

        //获取足够的金额
        //交易输入列表
        List<TransactionOutput> inputs = new ArrayList<>();
        List<String> inputPrivateKeyList = new ArrayList<>();
        //交易输入总金额
        long inputValues = 0;
        long feeValues = 0;
        boolean haveEnoughMoneyToPay = false;
        boolean haveFeeToPay = false;
        //序号
        for(String privateKey : privateKeyList){
            //TODO 优化 可能不止100
            List<TransactionOutput> utxoList = blockChainDataBase.queryUnspendTransactionOutputListByAddress(privateKey,0,100);
            for(TransactionOutput transactionOutput:utxoList){
                inputValues += transactionOutput.getValue();
                //交易输入
                inputs.add(transactionOutput);
                inputPrivateKeyList.add(privateKey);
                //大于的一部分可以用于交易手续费
                if(inputValues > outputVaules){
                    haveEnoughMoneyToPay = true;
                    feeValues = TransactionTool.calculateTransactionFee(inputs.size()+transactionOutputDtoList.size()+1);
                    if(feeValues < inputValues - outputVaules){
                        haveFeeToPay = true;
                        break;
                    }
                }
            }
        }

        if(!haveEnoughMoneyToPay){
            throw new ClassCastException("账户没有足够的金额去支付。");
        }
        if(!haveFeeToPay){
            throw new ClassCastException("账户没有足够的手续费去支付。");
        }

        //构建交易输入
        List<TransactionInputDTO> transactionInputDtoList = new ArrayList<>();
        for(TransactionOutput input:inputs){
            UnspendTransactionOutputDTO unspendTransactionOutputDto = Model2DtoTool.transactionOutput2UnspendTransactionOutputDto(input);
            TransactionInputDTO transactionInputDTO = new TransactionInputDTO();
            transactionInputDTO.setUnspendTransactionOutputDTO(unspendTransactionOutputDto);
            transactionInputDtoList.add(transactionInputDTO);
        }

        //构建交易
        TransactionDTO transactionDTO = new TransactionDTO();
        transactionDTO.setTransactionInputDtoList(transactionInputDtoList);
        transactionDTO.setTransactionOutputDtoList(transactionOutputDtoList);

        //校验校验手续费够不够，如果不够的话，继续补充交易手续费
        Transaction transaction = Dto2ModelTool.transactionDto2Transaction(blockChainDataBase,transactionDTO);
        if(TransactionTool.isTransactionFeeRight(transaction)){

        }


        //找零
        long change = inputValues - outputVaules - feeValues;
        if(change > 0){
            //创建新的账户，存放找零
            Account account = wallet.createAccount();
            wallet.addAccount(account);
            TransactionOutputDTO transactionOutputDTO = new TransactionOutputDTO();
            transactionOutputDTO.setValue(change);
            ScriptLock scriptLock = StackBasedVirtualMachine.createPayToPublicKeyHashOutputScript(account.getAddress());
            transactionOutputDTO.setScriptLockDTO(Model2DtoTool.scriptLock2ScriptLockDTO(scriptLock));
            transactionDTO.getTransactionOutputDtoList().add(transactionOutputDTO);
        }

        //签名
        for(int i=0;i<transactionInputDtoList.size();i++){
            String privateKey = inputPrivateKeyList.get(i);
            String publicKey = AccountUtil.accountFromPrivateKey(privateKey).getPublicKey();
            TransactionInputDTO transactionInputDTO = transactionInputDtoList.get(i);
            String signature = Model2DtoTool.signature(transactionDTO,privateKey);
            ScriptKey scriptKey = StackBasedVirtualMachine.createPayToPublicKeyHashInputScript(signature, publicKey);
            transactionInputDTO.setScriptKeyDTO(Model2DtoTool.scriptKey2ScriptKeyDTO(scriptKey));
        }
        return transactionDTO;
    }

    @Override
    public void submitTransaction(TransactionDTO transactionDTO) {
        miner.getMinerTransactionDtoDataBase().insertTransactionDTO(transactionDTO);
    }

    @Override
    public List<TransactionDTO> queryMiningTransactionList(long from,long size) {
        List<TransactionDTO> transactionDtoList = miner.getMinerTransactionDtoDataBase().selectTransactionDtoList(from,size);
        return transactionDtoList;
    }

    @Override
    public TransactionDTO queryMiningTransactionDtoByTransactionHash(String transactionHash) {
        TransactionDTO transactionDTO = miner.getMinerTransactionDtoDataBase().selectTransactionDtoByTransactionHash(transactionHash);
        return transactionDTO;
    }
}