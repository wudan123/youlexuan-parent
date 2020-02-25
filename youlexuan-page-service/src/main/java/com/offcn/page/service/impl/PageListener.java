package com.offcn.page.service.impl;

import com.offcn.page.service.ItemPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

@Component
public class PageListener implements MessageListener {

    @Autowired
    private ItemPageService itemPageService;

    @Override
    public void onMessage(Message message) {
        TextMessage textMessage=(TextMessage) message;
        try {
            String id =(String) textMessage.getText();
            System.out.println("PageListener接收到消息:"+id);
            Boolean b = itemPageService.getItemHtml(Long.parseLong(id));
            System.out.println("生成"+b);

        } catch (JMSException e) {
            e.printStackTrace();
        }

    }
}
