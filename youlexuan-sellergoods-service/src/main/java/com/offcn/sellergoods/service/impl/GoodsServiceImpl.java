package com.offcn.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.offcn.entity.PageResult;
import com.offcn.group.Goods;
import com.offcn.mapper.*;
import com.offcn.pojo.*;
import com.offcn.pojo.TbGoodsExample.Criteria;
import com.offcn.sellergoods.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class GoodsServiceImpl implements GoodsService {

	@Autowired
	private TbGoodsMapper goodsMapper;
	@Autowired
	private TbGoodsDescMapper tbGoodsDescMapper;
	@Autowired
	private TbBrandMapper brandMapper;
	@Autowired
	private TbItemCatMapper itemCatMapper;
	@Autowired
	private TbSellerMapper sellerMapper;
	@Autowired
	private TbItemMapper itemMapper;
	/**
	 * 查询全部
	 */
	@Override
	public List<TbGoods> findAll() {
		return goodsMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbGoods> page=   (Page<TbGoods>) goodsMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(Goods goods) {

		goods.getTbGoods().setAuditStatus("0");//设置添加时的状态

		goodsMapper.insert(goods.getTbGoods());

		goods.getTbGoodsDesc().setGoodsId(goods.getTbGoods().getId());//设置当前商品id

		tbGoodsDescMapper.insert(goods.getTbGoodsDesc());//插入商品扩展选项

		saveSku(goods);//插入商品sku列表数据




	}

	private void setItems(Goods goods,TbItem tbItem){

		tbItem.setGoodsId(goods.getTbGoods().getId());//商品id
		tbItem.setSeller(goods.getTbGoods().getSellerId());//商家id
		tbItem.setCategoryid(goods.getTbGoods().getCategory3Id());//分类编号
		tbItem.setUpdateTime(new Date());
		tbItem.setCreateTime(new Date());
		//设置品牌名称
		TbBrand brand = brandMapper.selectByPrimaryKey(goods.getTbGoods().getBrandId());//品牌对象
		tbItem.setBrand(brand.getName());

		//三级分类名称
		TbItemCat tbItemCat = itemCatMapper.selectByPrimaryKey(goods.getTbGoods().getCategory3Id());
		tbItem.setCategory(tbItemCat.getName());
		//设置商家店铺名称
		TbSeller seller = sellerMapper.selectByPrimaryKey(goods.getTbGoods().getSellerId());
		tbItem.setSeller(seller.getNickName());
		//设置图片地址
		List<Map> imagelist=JSON.parseArray(goods.getTbGoodsDesc().getItemImages(),Map.class);
		if (imagelist.size()>0){
			tbItem.setImage((String) imagelist.get(0).get("url"));//获取第一张图片url
		}

	}

	/**
	 * 插入sku列表数据
	 * @param goods
	 */
	private void saveSku(Goods goods){

		List<TbItem> itemList = goods.getItemList();

		if ("1".equals(goods.getTbGoods().getIsEnableSpec())){//是否启用规格

			for (TbItem tbItem : itemList) {
				String title = goods.getTbGoods().getGoodsName();//商品名称
				//spec:{"机身内存":"16G","网络":"移动3G"}
				Map<String,Object> map=JSON.parseObject(tbItem.getSpec());
				for (String key:map.keySet()){
					title+=" "+map.get(key);//标题后追加规格
				}
				tbItem.setTitle(title);
				setItems(goods,tbItem);//调用方法设置商品基础信息

				itemMapper.insert(tbItem);//添加商品:tb_item

			}
		}else {//不启用
			TbItem item=new TbItem();
			item.setTitle(goods.getTbGoods().getGoodsName());

			item.setPrice(goods.getTbGoods().getPrice());
			item.setStatus("1");
			item.setIsDefault("1");
			item.setNum(9999);
			item.setSpec("{}");

			setItems(goods,item);
			itemMapper.insert(item);

		}

	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(Goods goods){
		goods.getTbGoods().setAuditStatus("0");//执行修改状态要重置0

		goodsMapper.updateByPrimaryKey(goods.getTbGoods());//修改商品

		tbGoodsDescMapper.updateByPrimaryKey(goods.getTbGoodsDesc());//修改商品扩展表

		TbItemExample example=new TbItemExample();
		TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andGoodsIdEqualTo(goods.getTbGoods().getId());
		itemMapper.deleteByExample(example);//修改商品sku列表先根据商品id删除再重新插入

		saveSku(goods);//添加新的商品sku列表数据
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public Goods findOne(Long id){
		Goods goods=new Goods();
		TbGoods tbGoods=goodsMapper.selectByPrimaryKey(id);
		goods.setTbGoods(tbGoods);
		TbGoodsDesc goodsDesc = tbGoodsDescMapper.selectByPrimaryKey(id);
		goods.setTbGoodsDesc(goodsDesc);
		//更具商品id查询规格
		TbItemExample example=new TbItemExample();
		TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andGoodsIdEqualTo(tbGoods.getId());
		List<TbItem> tbItems = itemMapper.selectByExample(example);
		goods.setItemList(tbItems);

		return goods;
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			TbGoods goods = goodsMapper.selectByPrimaryKey(id);
			goods.setIsDelete("1");
			goodsMapper.updateByPrimaryKey(goods);
		}
		//修改商品sku状态为禁用
		List<TbItem> items = this.searchItemListByGoodsIdAndStatus(ids, "1");
		for (TbItem item : items) {
			item.setStatus("0");
			itemMapper.updateByPrimaryKey(item);
		}
	}
	
	
		@Override
	public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbGoodsExample example=new TbGoodsExample();
		Criteria criteria = example.createCriteria();
		
		if(goods!=null){			
						if(goods.getSellerId()!=null && goods.getSellerId().length()>0){
				criteria.andSellerIdEqualTo(goods.getSellerId());//按商家名称查询
			}			if(goods.getGoodsName()!=null && goods.getGoodsName().length()>0){
				criteria.andGoodsNameLike("%"+goods.getGoodsName()+"%");
			}			if(goods.getAuditStatus()!=null && goods.getAuditStatus().length()>0){
				criteria.andAuditStatusLike("%"+goods.getAuditStatus()+"%");
			}			if(goods.getIsMarketable()!=null && goods.getIsMarketable().length()>0){
				criteria.andIsMarketableLike("%"+goods.getIsMarketable()+"%");
			}			if(goods.getCaption()!=null && goods.getCaption().length()>0){
				criteria.andCaptionLike("%"+goods.getCaption()+"%");
			}			if(goods.getSmallPic()!=null && goods.getSmallPic().length()>0){
				criteria.andSmallPicLike("%"+goods.getSmallPic()+"%");
			}			if(goods.getIsEnableSpec()!=null && goods.getIsEnableSpec().length()>0){
				criteria.andIsEnableSpecLike("%"+goods.getIsEnableSpec()+"%");
			}			if(goods.getIsDelete()!=null && goods.getIsDelete().length()>0){
				criteria.andIsDeleteLike("%"+goods.getIsDelete()+"%");
			}
						criteria.andIsDeleteIsNull();
		}
		
		Page<TbGoods> page= (Page<TbGoods>)goodsMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public void updateStatus(Long[] ids, String status) {
		for (Long id : ids) {
			//商品spu状态
			TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
			tbGoods.setAuditStatus(status);
			goodsMapper.updateByPrimaryKey(tbGoods);

			//商品sku状态
			TbItemExample example=new TbItemExample();
			TbItemExample.Criteria criteria = example.createCriteria();
			criteria.andGoodsIdEqualTo(tbGoods.getId());
			List<TbItem> items = itemMapper.selectByExample(example);
			for (TbItem item : items) {
				item.setStatus("1");
				itemMapper.updateByPrimaryKey(item);
			}
		}


	}

	@Override
	public void isMarketable(Long[] ids, String isMarketable) {
		for (Long id : ids) {
			TbGoods goods = goodsMapper.selectByPrimaryKey(id);
			goods.setIsMarketable("1");
			goodsMapper.updateByPrimaryKey(goods);

			//商品sku状态为上架
			TbItemExample example=new TbItemExample();
			TbItemExample.Criteria criteria = example.createCriteria();
			criteria.andGoodsIdEqualTo(goods.getId());
			List<TbItem> items = itemMapper.selectByExample(example);
			for (TbItem item : items) {
				item.setStatus("1");
				itemMapper.updateByPrimaryKey(item);
			}
		}
	}

	/**
	 * 根据商品id和状态查询item列表
	 * @param goodsIds
	 * @param status
	 * @return
	 */
	@Override
	public List<TbItem> searchItemListByGoodsIdAndStatus(Long[] goodsIds, String status) {
		TbItemExample example=new TbItemExample();
		TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andGoodsIdIn(Arrays.asList(goodsIds));
		criteria.andStatusEqualTo(status);

		return itemMapper.selectByExample(example);
	}

}
