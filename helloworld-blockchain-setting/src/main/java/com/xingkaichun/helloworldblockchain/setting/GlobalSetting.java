package com.xingkaichun.helloworldblockchain.setting;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 全局设置
 *
 * @author 邢开春 微信HelloworldBlockchain 邮箱xingkaichun@qq.com
 */
public class GlobalSetting {

    //区块链的链ID
    public static final String BLOCK_CHAIN_ID = "0001";
    //区块链网络中的种子节点
    public static final List<String> SEED_NODE_LIST = Arrays.asList("139.9.125.122:8444","119.3.57.171:8444");


    /**
     * 系统版本
     */
    public static class SystemVersionConstant{
        /**
         * 区块链版本设置
         * 这里的版本是一个时间戳数值
         * 部分配置需要根据版本时间戳去获取
         * [最新版本时间]必须要在[当前时刻时间]之后，系统才能正常运行。
         * 例如：第一版本只支持运行至北京时间2020-06-01 00:00:00，到时间后必须升级系统。
         */
        //第一版本2020-06-01 00:00:00
        public static final long BLOCK_CHAIN_VERSION_1 = 1590940800000L;
        //第二版本2020-08-01 00:00:00
        public static final long BLOCK_CHAIN_VERSION_2 = 1596211200000L;
        //第二版本2020-10-01 00:00:00
        public static final long BLOCK_CHAIN_VERSION_3 = 1696211200000L;
        //版本列表
        public static final List<Long> BLOCK_CHAIN_VERSION_LIST =
                Collections.unmodifiableList(Arrays.asList(BLOCK_CHAIN_VERSION_1,BLOCK_CHAIN_VERSION_2,BLOCK_CHAIN_VERSION_3));

        /**
         * 检查系统版本是否支持。
         */
        public static boolean isVersionLegal(long timestamp){
            if(timestamp > BLOCK_CHAIN_VERSION_LIST.get(BLOCK_CHAIN_VERSION_LIST.size()-1)){
                return false;
            }
            return true;
        }
        /**
         * 获得系统版本。
         */
        public static long obtainVersion(){
            return BLOCK_CHAIN_VERSION_LIST.get(BLOCK_CHAIN_VERSION_LIST.size()-1);
        }
    }

    /**
     * 创世区块
     */
    public static class GenesisBlock{
        //创世区块的高度
        public static final long HEIGHT = 0;
        //创世区块的哈希
        public static final String HASH = "0000000000000000000000000000000000000000000000000000000000000000";
    }

    /**
     * 挖矿设置
     */
    public static class MinerConstant{
        //产生区块的平均时间
        public static final long GENERATE_BLOCK_AVERAGE_TIMESTAMP = 1000 *  60 * 2;
        //初始化产生区块的难度 默认初始难度 4G CPU 约 10分钟挖出区块的难度
        public static final String INIT_GENERATE_BLOCK_DIFFICULTY_STRING = "2FFF000000000000000000000000000000000000000000000000000000";
        //初始化挖矿激励金额
        public static final long INIT_MINE_BLOCK_INCENTIVE_COIN_AMOUNT = 50L;
        //挖矿激励减产周期
        public static final long MINE_BLOCK_INCENTIVE_REDUCE_BY_HALF_INTERVAL_TIMESTAMP = 1000 * 60 * 60 * 24;

        //每轮挖矿最大时长。挖矿时间太长，则新提交的交易就很延迟才能包含到区块里。
        public static final long MAX_MINE_TIMESTAMP = 1000 * 60 * 60;

        //交易最大滞后区块时间
        public static final long TRANSACTION_TIMESTAMP_MAX_AFTER_CURRENT_TIMESTAMP = 1000 * 60 * 60 * 24;
        //交易最大超前区块时间
        public static final long TRANSACTION_TIMESTAMP_MAX_BEFORE_CURRENT_TIMESTAMP = 1000 * 60 * 60 * 24;
    }

    /**
     * 区块设置
     */
    public static class BlockConstant {
        //交易文本字符串最大长度值
        public static final long TRANSACTION_TEXT_MAX_SIZE = 1024;
        //区块存储容量限制
        public static final long BLOCK_TEXT_MAX_SIZE = 1024 * 1024;
        //区块最多含有的交易数量
        public static final long BLOCK_MAX_TRANSACTION_SIZE = BLOCK_TEXT_MAX_SIZE/TRANSACTION_TEXT_MAX_SIZE;
        //nonce最大值
        public static final long MAX_NONCE = Long.MAX_VALUE;
        //nonce最小值
        public static final long MIN_NONCE = 0;

        //区块时间戳最小存储容量
        public static final long BLOCK_TEXT_TIMESTAMP_MIN_SIZE = 13;
        //区块时间戳最大存储容量
        public static final long BLOCK_TEXT_TIMESTAMP_MAX_SIZE = 13;
    }

    /**
     * 交易设置
     */
    public static class TransactionConstant{
        //最大交易金额
        public static final long TRANSACTION_MAX_AMOUNT = Long.MAX_VALUE;
        //最小交易金额
        public static final long TRANSACTION_MIN_AMOUNT = 1L;
        //最小交易手续费
        public static final long MIN_TRANSACTION_FEE = 1L;
        //最大交易金额
        public static final long TRANSACTION_MAX_INPUT_COUNT = 100L;
        //最小交易金额
        public static final long TRANSACTION_MAX_OUTPUT_COUNT = 100L;


        //交易金额最大存储容量
        public static final long TRANSACTION_TEXT_VALUE_MAX_SIZE = 20;
        //交易地址最小存储容量
        public static final long TRANSACTION_TEXT_ADDRESS_MIN_SIZE = 20;
        //交易地址最大存储容量
        public static final long TRANSACTION_TEXT_ADDRESS_MAX_SIZE = 40;
        //交易时间戳最小存储容量
        public static final long TRANSACTION_TEXT_TIMESTAMP_MIN_SIZE = 13;
        //交易时间戳最大存储容量
        public static final long TRANSACTION_TEXT_TIMESTAMP_MAX_SIZE = 13;
    }

    /**
     * 脚本设置
     */
    public static class ScriptConstant{
        //输入脚本最小存储容量
        public static final long SCRIPT_INPUT_TEXT_MAX_SIZE = 500;
        //输出脚本最大存储容量
        public static final long SCRIPT_OUTPUT_TEXT_MAX_SIZE = 500;

        //脚本操作码/操作数最小存储容量
        public static final long SCRIPT_OPERATION_TEXT_MIN_SIZE = 1;
        //脚本操作码/操作数最大存储容量
        public static final long SCRIPT_OPERATION_TEXT_MAX_SIZE = 64;
    }
}
