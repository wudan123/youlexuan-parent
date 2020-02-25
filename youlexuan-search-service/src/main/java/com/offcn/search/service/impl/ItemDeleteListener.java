package com.offcn.search.service.impl;

import com.offcn.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import java.util.Arrays;

@Component
public class ItemDeleteListener implements MessageListener {
    @Autowired
    private ItemSearchService itemSearchService;

    @Override
    public void onMessage(Message message) {
        ObjectMessage objectMessage=(ObjectMessage) message;
        try {
            Long[] ids=(Long[]) objectMessage.getObject();

            System.out.println("成功监听到消息为:"+ids);
            itemSearchService.deleteGoodsIds(Arrays.asList(ids));
            System.out.println("成功删除索引库!");
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
