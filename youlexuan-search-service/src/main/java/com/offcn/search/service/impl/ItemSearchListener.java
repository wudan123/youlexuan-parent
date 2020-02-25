package com.offcn.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.promeg.pinyinhelper.Pinyin;
import com.offcn.pojo.TbItem;
import com.offcn.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ItemSearchListener implements MessageListener {

    @Autowired
    private ItemSearchService itemSearchService;


    @Override
    public void onMessage(Message message) {
        System.out.println("接收导入solr数据请求");

        try {
            if (message instanceof TextMessage){
                TextMessage textMessage=(TextMessage) message;
                String jsonString = textMessage.getText();
                //将字符串专程list集合
                List<TbItem> itemList = JSON.parseArray(jsonString, TbItem.class);
                for (TbItem item : itemList) {
                    System.out.println("商品标题:"+item.getTitle());
                    //sku规格列表转化成json对象
                    Map<String,Object> specMap= JSON.parseObject(item.getSpec());
                    Map map=new HashMap();
                    for (String key : specMap.keySet()) {
                        map.put(Pinyin.toPinyin(key,"").toLowerCase(),specMap.get(key));

                    }
                    item.setSpecMap(map);

                }
                itemSearchService.importItemList(itemList);
                System.out.println("成功保存sku数据到solr");
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
