package com.offcn.page.service;

/**
 * 商品详情页接口
 */
public interface ItemPageService {
    /**
     * 生成商品详情页
     * @param goodsId
     * @return
     */
    public Boolean getItemHtml(Long goodsId);

    /**
     * 删除静态页面
     */
    public boolean deleteItemHtml(Long[] goodsIds);

}

