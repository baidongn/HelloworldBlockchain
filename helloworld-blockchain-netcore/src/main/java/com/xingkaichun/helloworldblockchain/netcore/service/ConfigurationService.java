package com.xingkaichun.helloworldblockchain.netcore.service;


import com.xingkaichun.helloworldblockchain.crypto.model.Account;
import com.xingkaichun.helloworldblockchain.netcore.dto.configuration.ConfigurationDto;

/**
 * 配置service
 *
 * @author 邢开春 微信HelloworldBlockchain 邮箱xingkaichun@qq.com
 */
public interface ConfigurationService {

    /**
     * 根据配置Key获取配置
     */
    ConfigurationDto getConfigurationByConfigurationKey(String confKey);

    /**
     * 设置配置
     */
    void setConfiguration(ConfigurationDto configurationDto);

    /**
     * 获取默认矿工账户
     */
    Account getDefaultMinerAccount();

    /**
     * 获取矿工账户地址
     * 如果有用户设置矿工账户地址，则返回用户设置的矿工账户地址；
     * 否则，返回默认矿工账户账户地址
     */
    String getMinerAddress();


    /**
     * 是否自动搜寻区块链网络节点
     */
    boolean autoSearchNodeOption();
}
