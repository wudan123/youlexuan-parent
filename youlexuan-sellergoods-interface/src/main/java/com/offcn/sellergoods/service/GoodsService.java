package com.offcn.sellergoods.service;

import com.offcn.entity.PageResult;
import com.offcn.group.Goods;
import com.offcn.pojo.TbGoods;
import com.offcn.pojo.TbItem;

import java.util.List;

/**
 * 服务层接口
 * @author Administrator
 *
 */
public interface GoodsService {

	/**
	 * 返回全部列表
	 * @return
	 */
	public List<TbGoods> findAll();
	
	
	/**
	 * 返回分页列表
	 * @return
	 */
	public PageResult findPage(int pageNum, int pageSize);


	/**
	 * 增加 Goods自定义复合实体类
	*/
	public void add(Goods goods);


	/**
	 * 修改
	 */
	public void update(Goods goods);


	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	public Goods findOne(Long id);


	/**
	 * 批量删除
	 * @param ids
	 */
	public void delete(Long[] ids);

	/**
	 * 分页
	 * @param pageNum 当前页 码
	 * @param pageSize 每页记录数
	 * @return
	 */
	public PageResult findPage(TbGoods goods, int pageNum, int pageSize);


	/**
	 * 修改商品状态
	 * @param ids
	 * @param status
	 */
	public void updateStatus(Long []ids,String status);


	/**
	 * 商品是否上架
	 * @param ids
	 * @param isMarketable
	 */
	public void isMarketable(Long []ids,String isMarketable);


	/**
	 * 根据商品表id和状态查询item表
	 */
	public List<TbItem> searchItemListByGoodsIdAndStatus(Long [] goodsIds,String status);
	
}
