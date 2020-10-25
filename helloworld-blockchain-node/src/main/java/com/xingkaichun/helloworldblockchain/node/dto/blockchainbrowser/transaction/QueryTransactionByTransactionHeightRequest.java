package com.xingkaichun.helloworldblockchain.node.dto.blockchainbrowser.transaction;

import com.xingkaichun.helloworldblockchain.netcore.dto.common.page.PageCondition;

/**
 *
 * @author 邢开春 微信HelloworldBlockchain 邮箱xingkaichun@qq.com
 */
public class QueryTransactionByTransactionHeightRequest {

    private PageCondition pageCondition;




    //region get set

    public PageCondition getPageCondition() {
        return pageCondition;
    }

    public void setPageCondition(PageCondition pageCondition) {
        this.pageCondition = pageCondition;
    }

    //endregion
}
