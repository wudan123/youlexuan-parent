package com.offcn.solrUtil;

import com.alibaba.fastjson.JSON;
import com.github.promeg.pinyinhelper.Pinyin;
import com.offcn.mapper.TbItemMapper;
import com.offcn.pojo.TbItem;
import com.offcn.pojo.TbItemExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SolrUtil {

    @Autowired
    private TbItemMapper tbItemMapper;
    @Autowired
    private SolrTemplate solrTemplate;

    public void importData(){
        TbItemExample example=new TbItemExample();
        TbItemExample.Criteria criteria = example.createCriteria();
        criteria.andStatusEqualTo("1");//导入数据有效数据
        List<TbItem> tbItemList = tbItemMapper.selectByExample(example);
        for (TbItem item : tbItemList) {
            System.out.println("商品品牌------"+item.getBrand());
            Map<String,String> map=JSON.parseObject(item.getSpec(),Map.class);
            Map<String,String> pinyinMap=new HashMap<>();//创建新的map存储拼音
            for (String key : map.keySet()) {
                pinyinMap.put(Pinyin.toPinyin(key,"").toLowerCase(),map.get(key));//key转化为拼音
            }
            item.setSpecMap(pinyinMap);//将拼音map集合添加到item对象中
        }
        solrTemplate.saveBeans(tbItemList);//数据导入到solr
        solrTemplate.commit();
        System.out.println("添加到solr成功！");
    }

    public static void main(String[] args) {
        ApplicationContext context=new ClassPathXmlApplicationContext("classpath*:spring/applicationContext-*.xml");
        SolrUtil solrUtil = (SolrUtil) context.getBean("solrUtil");
        solrUtil.importData();


    }
}
