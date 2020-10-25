package com.xingkaichun.helloworldblockchain.node.dto.blockchainbrowser.transaction;

import com.xingkaichun.helloworldblockchain.netcore.dto.transaction.SubmitTransactionDto;

/**
 *
 * @author 邢开春 微信HelloworldBlockchain 邮箱xingkaichun@qq.com
 */
public class SubmitTransactionRequest {

    private SubmitTransactionDto submitTransactionDto;




    //region get set

    public SubmitTransactionDto getSubmitTransactionDto() {
        return submitTransactionDto;
    }

    public void setSubmitTransactionDto(SubmitTransactionDto submitTransactionDto) {
        this.submitTransactionDto = submitTransactionDto;
    }

    //endregion
}
