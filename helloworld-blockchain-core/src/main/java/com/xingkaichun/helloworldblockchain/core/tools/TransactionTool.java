package com.xingkaichun.helloworldblockchain.core.tools;

import com.google.common.primitives.Bytes;
import com.xingkaichun.helloworldblockchain.core.model.script.Script;
import com.xingkaichun.helloworldblockchain.core.model.script.ScriptExecuteResult;
import com.xingkaichun.helloworldblockchain.core.model.transaction.Transaction;
import com.xingkaichun.helloworldblockchain.core.model.transaction.TransactionInput;
import com.xingkaichun.helloworldblockchain.core.model.transaction.TransactionOutput;
import com.xingkaichun.helloworldblockchain.core.model.transaction.TransactionType;
import com.xingkaichun.helloworldblockchain.core.script.StackBasedVirtualMachine;
import com.xingkaichun.helloworldblockchain.core.utils.ByteUtil;
import com.xingkaichun.helloworldblockchain.crypto.AccountUtil;
import com.xingkaichun.helloworldblockchain.crypto.HexUtil;
import com.xingkaichun.helloworldblockchain.crypto.SHA256Util;
import com.xingkaichun.helloworldblockchain.netcore.transport.dto.TransactionDTO;
import com.xingkaichun.helloworldblockchain.netcore.transport.dto.TransactionInputDTO;
import com.xingkaichun.helloworldblockchain.netcore.transport.dto.TransactionOutputDTO;
import com.xingkaichun.helloworldblockchain.netcore.transport.dto.UnspendTransactionOutputDto;
import com.xingkaichun.helloworldblockchain.setting.GlobalSetting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Transaction工具类
 *
 * @author 邢开春 微信HelloworldBlockchain 邮箱xingkaichun@qq.com
 */
public class TransactionTool {

    private static final Logger logger = LoggerFactory.getLogger(TransactionTool.class);

    /**
     * 交易输入总额
     */
    public static long getInputsValue(Transaction transaction) {
        return getInputsValue(transaction.getInputs());
    }
    /**
     * 交易输入总额
     */
    public static long getInputsValue(List<TransactionInput> inputs) {
        long total = 0;
        if(inputs != null){
            for(TransactionInput input : inputs) {
                total += input.getUnspendTransactionOutput().getValue();
            }
        }
        return total;
    }



    /**
     * 交易输出总额
     */
    public static long getOutputsValue(Transaction transaction) {
        return getOutputsValue(transaction.getOutputs());
    }
    /**
     * 交易输出总额
     */
    public static long getOutputsValue(List<TransactionOutput> outputs) {
        long total = 0;
        if(outputs != null){
            for(TransactionOutput o : outputs) {
                total += o.getValue();
            }
        }
        return total;
    }



    /**
     * 获取用于签名的交易数据
     */
    public static String getSignatureData(Transaction transaction) {
        String data = transaction.getTransactionHash();
        return data;
    }

    /**
     * 交易签名
     */
    public static String signature(String privateKey, Transaction transaction) {
        String strSignature = AccountUtil.signature(privateKey, getSignatureData(transaction));
        return strSignature;
    }

    /**
     * 验证脚本
     */
    public static boolean verifyScript(Transaction transaction) {
        try{
            List<TransactionInput> inputs = transaction.getInputs();
            if(inputs != null && inputs.size()!=0){
                for(TransactionInput transactionInput:inputs){
                    Script payToClassicAddressScript = StackBasedVirtualMachine.createPayToClassicAddressScript(transactionInput.getScriptKey(),transactionInput.getUnspendTransactionOutput().getScriptLock());
                    StackBasedVirtualMachine stackBasedVirtualMachine = new StackBasedVirtualMachine();
                    ScriptExecuteResult scriptExecuteResult = stackBasedVirtualMachine.executeScript(transaction,payToClassicAddressScript);
                    if(scriptExecuteResult.size()!=1 || !Boolean.valueOf(scriptExecuteResult.pop())){
                        return false;
                    }
                }
            }
        }catch (Exception e){
            logger.debug("交易校验失败：交易脚本钥匙解锁交易脚本锁异常。",e);
            return false;
        }
        return true;
    }

    /**
     * 计算交易哈希
     */
    public static String calculateTransactionHash(Transaction transaction){
        return calculateTransactionHash(NodeTransportDtoTool.classCast(transaction));
    }

    /**
     * 计算交易哈希
     */
    public static String calculateTransactionHash(TransactionDTO transactionDTO){
        byte[] bytesTransaction = bytesTransaction(transactionDTO);
        byte[] sha256Digest = SHA256Util.digest(bytesTransaction);
        return HexUtil.bytesToHexString(sha256Digest);
    }

    /**
     * 字节型脚本
     */
    public static byte[] bytesTransaction(TransactionDTO transactionDTO) {
        List<byte[]> bytesTransactionInputList = new ArrayList<>();
        List<TransactionInputDTO> inputs = transactionDTO.getInputs();
        if(inputs != null && inputs.size()!=0){
            for(TransactionInputDTO transactionInputDTO:inputs){
                byte[] bytesTransactionInput = bytesTransactionInput(transactionInputDTO);
                bytesTransactionInputList.add(bytesTransactionInput);
            }
        }
        List<byte[]> bytesTransactionOutputList = new ArrayList<>();
        List<TransactionOutputDTO> outputs = transactionDTO.getOutputs();
        for(TransactionOutputDTO transactionOutputDTO:outputs){
            byte[] bytesTransactionOutput = bytesTransactionOutput(transactionOutputDTO);
            bytesTransactionOutputList.add(bytesTransactionOutput);
        }

        byte[] data = Bytes.concat(ByteUtil.concatLengthBytes(bytesTransactionInputList),
                ByteUtil.concatLengthBytes(bytesTransactionOutputList));
        return data;
    }

    /**
     * 计算交易输出哈希
     */
    public static String calculateTransactionOutputHash(TransactionOutput output) {
        return calculateTransactionOutputHash(output.getTransactionOutputSequence(),output.getValue(),output.getScriptLock());
    }

    /**
     * 计算交易输出哈希
     */
    public static String calculateTransactionOutputHash(long transactionOutputSequence, TransactionOutputDTO transactionOutputDTO) {
        return calculateTransactionOutputHash(transactionOutputSequence,transactionOutputDTO.getValue(),transactionOutputDTO.getScriptLock());
    }

    /**
     * 计算交易输出哈希
     */
    private static String calculateTransactionOutputHash(long transactionOutputSequence, long value, List<String> scriptLock) {
        byte[] data = bytesTransactionOutput(transactionOutputSequence,value,scriptLock);
        byte[] sha256Digest = SHA256Util.digest(data);
        return HexUtil.bytesToHexString(sha256Digest);
    }


    /**
     * 交易中的金额是否符合系统的约束
     */
    public static boolean isTransactionAmountLegal(Transaction transaction) {
        List<TransactionOutput> outputs = transaction.getOutputs();
        if(outputs != null){
            for(TransactionOutput output:outputs){
                if(!isTransactionAmountLegal(output.getValue())){
                    logger.debug("交易金额不合法");
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 是否是一个合法的交易金额：这里用于限制交易金额的最大值、最小值、小数保留位置
     */
    public static boolean isTransactionAmountLegal(long transactionAmount) {
        try {
            //交易金额不能小于等于0
            if(transactionAmount <= 0){
                logger.debug("交易金额不合法：交易金额不能小于等于0");
                return false;
            }
            //校验交易金额最小值
            if(transactionAmount < GlobalSetting.TransactionConstant.TRANSACTION_MIN_AMOUNT){
                logger.debug("交易金额不合法：交易金额不能小于系统默认交易金额最小值");
                return false;
            }
            //校验交易金额最大值
            if(transactionAmount > GlobalSetting.TransactionConstant.TRANSACTION_MAX_AMOUNT){
                logger.debug("交易金额不合法：交易金额不能大于系统默认交易金额最大值");
                return false;
            }
            return true;
        } catch (Exception e) {
            logger.debug("校验金额方法出现异常，请检查。",e);
            return false;
        }
    }

    /**
     * 校验激励
     */
    public static boolean isIncentiveRight(long targetMinerReward, Transaction transaction) {
        if(transaction.getTransactionType() != TransactionType.COINBASE){
            logger.debug("区块数据异常，区块中的第一笔交易应当是挖矿奖励交易。");
            return false;
        }
        List<TransactionInput> inputs = transaction.getInputs();
        List<TransactionOutput> outputs = transaction.getOutputs();
        if(inputs != null && inputs.size()!=0){
            logger.debug("区块数据异常，挖矿奖励交易交易输入应当是空。");
            return false;
        }
        if(outputs == null || outputs.size()!=1){
            logger.debug("区块数据异常，挖矿奖励交易只能有一个交易输出。");
            return false;
        }
        if(targetMinerReward < outputs.get(0).getValue()){
            logger.debug("挖矿奖励数据异常，挖矿奖励金额大于系统核算奖励金额。");
            return false;
        }
        return true;
    }

    /**
     * 字节型交易输入
     */
    private static byte[] bytesTransactionInput(TransactionInputDTO transactionInputDTO) {
        UnspendTransactionOutputDto unspendTransactionOutputDto = transactionInputDTO.getUnspendTransactionOutputDto();
        List<String> scriptKey = transactionInputDTO.getScriptKey();

        byte[] bytesUnspendTransactionOutput = bytesUnspendTransactionOutput(unspendTransactionOutputDto);
        byte[] bytesScriptKey = ScriptTool.bytesScript(scriptKey);

        byte[] data = Bytes.concat(ByteUtil.concatLengthBytes(bytesUnspendTransactionOutput),
                ByteUtil.concatLengthBytes(bytesScriptKey));
        return data;
    }

    /**
     * 字节型交易输入
     */
    private static byte[] bytesUnspendTransactionOutput(UnspendTransactionOutputDto unspendTransactionOutputDto) {
        String transactionHash = unspendTransactionOutputDto.getTransactionHash();
        long transactionOutputIndex = unspendTransactionOutputDto.getTransactionOutputIndex();

        byte[] bytesTransactionHash = HexUtil.hexStringToBytes(transactionHash);
        byte[] bytesTransactionOutputIndex = ByteUtil.longToBytes8(transactionOutputIndex);

        byte[] data = Bytes.concat(ByteUtil.concatLengthBytes(bytesTransactionHash),
                ByteUtil.concatLengthBytes(bytesTransactionOutputIndex));
        return data;
    }

    /**
     * 字节型交易输出
     */
    public static byte[] bytesTransactionOutput(TransactionOutputDTO transactionOutputDTO) {
        long value = transactionOutputDTO.getValue();
        List<String> scriptLock = transactionOutputDTO.getScriptLock();

        byte[] bytesValue = ByteUtil.longToBytes8(value);
        byte[] bytesScriptLock = ScriptTool.bytesScript(scriptLock);

        byte[] data = Bytes.concat(ByteUtil.concatLengthBytes(bytesValue),
                ByteUtil.concatLengthBytes(bytesScriptLock));
        return data;
    }
    /**
     * 字节型交易输出 TODO 需要加交易输出哈希
     */
    public static byte[] bytesTransactionOutput(long transactionOutputSequence, long value, List<String> scriptLock) {
        byte[] bytesTransactionOutputSequence = ByteUtil.longToBytes8(transactionOutputSequence);
        byte[] bytesValue = ByteUtil.longToBytes8(value);
        byte[] bytesScriptLock = ScriptTool.bytesScript(scriptLock);

        byte[] data = Bytes.concat(ByteUtil.concatLengthBytes(bytesTransactionOutputSequence),
                ByteUtil.concatLengthBytes(bytesValue),
                ByteUtil.concatLengthBytes(bytesScriptLock));
        return data;
    }

    /**
     * 是否存在重复的交易输入
     */
    public static boolean isExistDuplicateTransactionInput(Transaction transaction) {
        List<TransactionInput> inputs = transaction.getInputs();
        if(inputs == null || inputs.size()==0){
            return false;
        }
        Set<String> hashSet = new HashSet<>();
        for(TransactionInput transactionInput : inputs) {
            TransactionOutput unspendTransactionOutput = transactionInput.getUnspendTransactionOutput();
            String unspendTransactionOutputHash = unspendTransactionOutput.getTransactionOutputHash();
            if(hashSet.contains(unspendTransactionOutputHash)){
                return true;
            }
            hashSet.add(unspendTransactionOutputHash);
        }
        return false;
    }

    /**
     * 交易新产生的哈希是否存在重复
     */
    public static boolean isExistDuplicateNewHash(Transaction transaction) {
        String transactionHash = transaction.getTransactionHash();
        //校验：只从交易对象层面校验，交易中新产生的哈希是否有重复
        Set<String> hashSet = new HashSet<>();
        if(hashSet.contains(transactionHash)){
            return false;
        }else {
            hashSet.add(transactionHash);
        }
        List<TransactionOutput> outputs = transaction.getOutputs();
        if(outputs != null){
            for(TransactionOutput transactionOutput : outputs) {
                String transactionOutputHash = transactionOutput.getTransactionOutputHash();
                if(hashSet.contains(transactionOutputHash)){
                    return false;
                }else {
                    hashSet.add(transactionOutputHash);
                }
            }
        }
        return true;
    }

    /**
     * 转账手续费是否正确
     */
    public static boolean isTransactionFeeRight(Transaction transaction) {
        long inputsValue = TransactionTool.getInputsValue(transaction);
        long outputsValue = TransactionTool.getOutputsValue(transaction);
        long fee = inputsValue - outputsValue;
        long targetFee = calculateTransactionFee(transaction);
        //交易手续费
        if(fee < targetFee){
            logger.debug(String.format("交易校验失败：交易手续费小于计算的最小手续费。"));
            return false;
        }
        return true;
    }

    /**
     * 计算转账手续费
     */
    public static long calculateTransactionFee(Transaction transaction) {
        long transactionTextSize = StructureSizeTool.calculateTransactionTextSize(transaction);
        long fee = transactionTextSize%GlobalSetting.TransactionConstant.TRANSACTION_FEE_PER_100==0?transactionTextSize/GlobalSetting.TransactionConstant.TRANSACTION_FEE_PER_100:transactionTextSize/GlobalSetting.TransactionConstant.TRANSACTION_FEE_PER_100+1;
        if(fee < GlobalSetting.TransactionConstant.MIN_TRANSACTION_FEE){
            fee =  GlobalSetting.TransactionConstant.MIN_TRANSACTION_FEE;
        }
        return fee;
    }

    /**
     * 交易输入必须要大于交易输出
     */
    public static boolean isTransactionInputsGreatEqualThanOutputsRight(Transaction transaction) {
        long inputsValue = TransactionTool.getInputsValue(transaction);
        long outputsValue = TransactionTool.getOutputsValue(transaction);
        if(inputsValue < outputsValue) {
            logger.debug("交易校验失败：交易的输入必须大于等于交易的输出。不合法的交易。");
            return false;
        }
        return true;
    }
}
