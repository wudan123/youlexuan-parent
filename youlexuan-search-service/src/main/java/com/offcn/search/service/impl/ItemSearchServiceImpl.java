package com.offcn.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.promeg.pinyinhelper.Pinyin;
import com.offcn.pojo.TbItem;
import com.offcn.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.*;

@Service(timeout = 3000)
public class ItemSearchServiceImpl implements ItemSearchService {
    @Autowired
    private SolrTemplate solrTemplate;
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public Map<String, Object> search(Map searchMap) {

        Map<String,Object> map=new HashMap<>();//创建map用于存储条件查询后的数据

        //查询列表
        map.putAll(searchList(searchMap));

        //查询分类
        List categoryList = searchCategoryList(searchMap);
        map.put("categoryList",categoryList);


        //根据商品类目查询对应的品牌和规格
        String categoryName =(String) searchMap.get("category");
        if (!"".equals(categoryName)){
            map.putAll(searchBrandAndSpecList(categoryName));
        }else {
            if (categoryList.size()>0){
                map.putAll(searchBrandAndSpecList((String)categoryList.get(0)));
            }
        }

//        if (categoryList.size()>0){
//            map.putAll(searchBrandAndSpecList((String) categoryList.get(0)));
//
//        }

        return map;
    }

    /**
     * 导入数据到solr 更新索引库
     * @param list
     */
    @Override
    public void importItemList(List<TbItem> list) {
        for (TbItem item : list) {
            //提取规格json字符串转化成map
            Map<String,String> specMap = JSON.parseObject(item.getSpec(), Map.class);
            Map map=new HashMap();
            for (String key : specMap.keySet()) {
                map.put("item_spec_"+Pinyin.toPinyin(key,"").toLowerCase(),specMap.get(key));
            }
            item.setSpecMap(map);
        }
        solrTemplate.saveBeans(list);//将item集合导入solr中
        solrTemplate.commit();

    }

    @Override
    public void deleteGoodsIds(List goodsList) {
        System.out.println("商品id"+goodsList);
        Query query = new SimpleQuery();
        Criteria criteria = new Criteria("item_goods_id").in(goodsList);
        query.addCriteria(criteria);
        solrTemplate.delete(query);
        solrTemplate.commit();
        System.out.println("成功删除索引库!");
    }

    private Map searchList(Map searchMap){

        Map map=new HashMap();
        //创建高亮的查询对象
        SimpleHighlightQuery query = new SimpleHighlightQuery();
        HighlightOptions options=new HighlightOptions();
        options.addField("item_title");
        options.setSimplePrefix("<em style='color:red'>");//设置标题前缀
        options.setSimplePostfix("</em>");//标题后缀
        query.setHighlightOptions(options);//高亮选项添加进高亮查询对象中

        // Query query = new SimpleQuery();

        //处理关键字空格问题
        String keywords =(String) searchMap.get("keywords");
        searchMap.put("keywords", keywords.replace(" ",""));
        System.out.println(searchMap.get("keywords"));

        //设置条件 关键字查询
        Criteria criteria=new Criteria("item_keywords").is(searchMap.get("keywords"));

        query.addCriteria(criteria);

        //按分类筛选
        if (!"".equals(searchMap.get("category"))){
            Criteria filtercriteria = new Criteria("item_category").is(searchMap.get("category"));
            SimpleFilterQuery filterQuery = new SimpleFilterQuery(filtercriteria);//过滤查询对象
            query.addFilterQuery(filterQuery);

        }
        //按品牌筛选
        if (!"".equals(searchMap.get("brand"))){
            Criteria filtercriteria = new Criteria("item_brand").is(searchMap.get("brand"));
            SimpleFilterQuery filterQuery = new SimpleFilterQuery(filtercriteria);//过滤查询对象
            query.addFilterQuery(filterQuery);

        }
        //按规格筛选
        if (searchMap.get("spec")!=null){
           Map<String,String> specMap=(Map) searchMap.get("spec");
            for (String key : specMap.keySet()) {
                //将规格对象中的key转化为拼音
                Criteria filterCriteria = new Criteria("item_spec_" + Pinyin.toPinyin(key, "").toLowerCase()).is(specMap.get(key));
                SimpleFilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
           
        }
        // 按商品价格筛选
        if (!"".equals(searchMap.get("price")) && searchMap.get("price")!=null){
            String[] priceArr = ((String) searchMap.get("price")).split("-");
            System.out.println(priceArr.toString());
            //如果价格区间起始不是0
            if (!priceArr[0].equals("0")){
                Criteria filterCriteria = new Criteria("item_price").greaterThanEqual(priceArr[0]);
                FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
            if (!priceArr[1].equals("*")){
                Criteria filterCriteria = new Criteria("item_price").lessThanEqual(priceArr[1]);
                FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
        }

        //分页查询
        Integer pageNo =(Integer) searchMap.get("pageNo");//当前页
        if (pageNo==null){
            pageNo=1;//默认为第一页
        }
        Integer pageSize =(Integer) searchMap.get("pageSize");//每页商品条数
        if (pageSize==null){
            pageSize=10;//默认展示10条数据
        }
        query.setOffset((pageNo-1)*pageSize);//从第几条记录开始查
        query.setRows(pageSize);//每页展示条数


        //按字段排序
        String sort =(String) searchMap.get("sort");//ASC 或DESC
        String sortField =(String) searchMap.get("sortField");//排序字段
        if (!"".equals(sort) && sort!=null){
            if (sort.equals("ASC")){
                Sort s = new Sort(Sort.Direction.ASC, "item_" + sortField);
                query.addSort(s);
            }
            if (sort.equals("DESC")){
                Sort s = new Sort(Sort.Direction.DESC, "item_" + sortField);
                query.addSort(s);
            }
        }


        HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);

        //获取高亮数据集合入口
        List<HighlightEntry<TbItem>> highlightEntryList = page.getHighlighted();

        for (HighlightEntry<TbItem> entry : highlightEntryList) {
            TbItem item = entry.getEntity();//获取数据对象

            if (entry.getHighlights().size()>0 && entry.getHighlights().get(0).getSnipplets().size()>0){
                List<HighlightEntry.Highlight> entryHighlights = entry.getHighlights();//获取高亮集合
                List<String> snipplets = entryHighlights.get(0).getSnipplets();
                item.setTitle(snipplets.get(0));//获取第一个高亮字段，设置到标题
            }

        }
        map.put("rows",page.getContent());
        map.put("total",page.getTotalElements());//总记录数
        map.put("totalPage",page.getTotalPages());//总页数

        return map;
    }

    private List searchCategoryList(Map searchMap){
        List list=new ArrayList();
        Query query = new SimpleQuery();
        //根据关键字查询
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);

        //设置分组选项
        GroupOptions groupOptions = new GroupOptions().addGroupByField("item_category");
        query.setGroupOptions(groupOptions);

        //获取分组页
        GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query, TbItem.class);
        GroupResult<TbItem> result = page.getGroupResult("item_category");//获取分组结果
        Page<GroupEntry<TbItem>> entries = result.getGroupEntries();//得到分组入口页
        List<GroupEntry<TbItem>> content = entries.getContent();//入口结果集
        for (GroupEntry<TbItem> entry : content) {
            System.out.println("分类名称："+entry.getGroupValue());
            list.add(entry.getGroupValue());
        }

        return list;

    }

    /**
     * 搜索品牌列表和规格列表
     * @param category
     * @return
     */
    private Map searchBrandAndSpecList(String category){
        Map map=new HashMap();
        //获取模板id
        Long typeId = (Long) redisTemplate.boundHashOps("itemCat").get(category);

        if (typeId!=null){
            List brandList =(List) redisTemplate.boundHashOps("brandList").get(typeId);
            map.put("brandList",brandList);

            List specList =(List) redisTemplate.boundHashOps("specList").get(typeId);
            map.put("specList",specList);
        }

        return map;
    }
}
