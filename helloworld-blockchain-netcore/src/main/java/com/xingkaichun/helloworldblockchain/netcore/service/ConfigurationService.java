package com.xingkaichun.helloworldblockchain.netcore.service;


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
     * 是否自动搜寻区块链网络节点
     */
    boolean autoSearchNodeOption();
}
