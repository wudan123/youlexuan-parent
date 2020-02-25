package com.offcn.search.service;

import com.offcn.pojo.TbItem;

import java.util.List;
import java.util.Map;

public interface ItemSearchService {


    public Map<String,Object> search(Map searchMap);


    /**
     * 更新solr索引库 导入item列表
     */
    public void importItemList(List<TbItem> list);

    public void deleteGoodsIds(List goodsList);
}
