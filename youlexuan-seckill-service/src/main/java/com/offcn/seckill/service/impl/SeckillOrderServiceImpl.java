package com.offcn.seckill.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.offcn.entity.PageResult;
import com.offcn.mapper.TbSeckillGoodsMapper;
import com.offcn.mapper.TbSeckillOrderMapper;
import com.offcn.pojo.TbSeckillGoods;
import com.offcn.pojo.TbSeckillOrder;
import com.offcn.pojo.TbSeckillOrderExample;
import com.offcn.pojo.TbSeckillOrderExample.Criteria;
import com.offcn.seckill.service.SeckillOrderService;
import com.offcn.utils.IdWorker;
import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;

import java.util.Date;
import java.util.List;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class SeckillOrderServiceImpl implements SeckillOrderService {

	@Autowired
	private TbSeckillOrderMapper seckillOrderMapper;
	@Autowired
	private TbSeckillGoodsMapper seckillGoodsMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbSeckillOrder> findAll() {
		return seckillOrderMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbSeckillOrder> page=   (Page<TbSeckillOrder>) seckillOrderMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbSeckillOrder seckillOrder) {
		seckillOrderMapper.insert(seckillOrder);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbSeckillOrder seckillOrder){
		seckillOrderMapper.updateByPrimaryKey(seckillOrder);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbSeckillOrder findOne(Long id){
		return seckillOrderMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			seckillOrderMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbSeckillOrder seckillOrder, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbSeckillOrderExample example=new TbSeckillOrderExample();
		Criteria criteria = example.createCriteria();
		
		if(seckillOrder!=null){			
						if(seckillOrder.getUserId()!=null && seckillOrder.getUserId().length()>0){
				criteria.andUserIdLike("%"+seckillOrder.getUserId()+"%");
			}			if(seckillOrder.getSellerId()!=null && seckillOrder.getSellerId().length()>0){
				criteria.andSellerIdLike("%"+seckillOrder.getSellerId()+"%");
			}			if(seckillOrder.getStatus()!=null && seckillOrder.getStatus().length()>0){
				criteria.andStatusLike("%"+seckillOrder.getStatus()+"%");
			}			if(seckillOrder.getReceiverAddress()!=null && seckillOrder.getReceiverAddress().length()>0){
				criteria.andReceiverAddressLike("%"+seckillOrder.getReceiverAddress()+"%");
			}			if(seckillOrder.getReceiverMobile()!=null && seckillOrder.getReceiverMobile().length()>0){
				criteria.andReceiverMobileLike("%"+seckillOrder.getReceiverMobile()+"%");
			}			if(seckillOrder.getReceiver()!=null && seckillOrder.getReceiver().length()>0){
				criteria.andReceiverLike("%"+seckillOrder.getReceiver()+"%");
			}			if(seckillOrder.getTransactionId()!=null && seckillOrder.getTransactionId().length()>0){
				criteria.andTransactionIdLike("%"+seckillOrder.getTransactionId()+"%");
			}	
		}
		
		Page<TbSeckillOrder> page= (Page<TbSeckillOrder>)seckillOrderMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Autowired
	private RedisTemplate redisTemplate;
	@Autowired
	private IdWorker idWorker;
	@Override
	public void submitOrder(Long seckillId, String userId) {
		//允许redis使用事务
		redisTemplate.setEnableTransactionSupport(true);
		redisTemplate.execute(new SessionCallback() {
			@Override
			public Object execute(RedisOperations redisOperations) throws DataAccessException {
				redisOperations.watch("seckillGoods");
				//开启事务之前查询秒杀商品对象
				TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(seckillId);
				//开启事务
				redisOperations.multi();
				//必要的空查询
				redisTemplate.boundHashOps("seckillGoods").get(seckillId);
				if (seckillGoods==null){
					redisOperations.exec();
					throw new RuntimeException("秒杀商品不存在!");
				}
				//获取商品库存
				if (seckillGoods.getStockCount()<=0){
					redisOperations.exec();
					System.out.println("商品被抢购一空");
				}
				//如果用户抢到商品，扣除缓存中的库存
				seckillGoods.setStockCount(seckillGoods.getStockCount()-1);
				//更新缓存
				redisTemplate.boundHashOps("seckillGoods").put(seckillId,seckillGoods);
				//判断库存等于0,清除商品信息，保存商品数据到数据库
				if (seckillGoods.getStockCount()==0){
					seckillGoodsMapper.updateByPrimaryKey(seckillGoods);
					System.out.println("保存秒杀商品到数据库");
					//清除缓存
					redisTemplate.boundHashOps("seckillGoods").delete(seckillId);
					System.out.println("秒杀商品对象缓存清除成功");
				}
				//生成订单信息到redis
				TbSeckillOrder seckillOrder=new TbSeckillOrder();
				seckillOrder.setId(idWorker.nextId());
				seckillOrder.setCreateTime(new Date());
				seckillOrder.setStatus("0");
				seckillOrder.setReceiver(userId);//买家
				seckillOrder.setSellerId(seckillGoods.getSellerId());//卖家
				seckillOrder.setMoney(seckillGoods.getCostPrice());//秒杀价
				redisTemplate.boundHashOps("seckillOrder").put(userId,seckillOrder);
				System.out.println("生成秒杀订单信息到redis中");
				//提交事务
				return redisOperations.exec();
			}
		});

	}

	@Override
	public TbSeckillOrder searchOrderFromRedisByUserId(String userId) {

		return (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
	}

	@Override
	public void saveOrderFromRedisToDb(String userId, Long orderId, String transactionId) {
		System.out.println("保存秒杀订单到数据库 用户id："+userId);
		//根据userId查询订单对象
		TbSeckillOrder order=(TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
		if (order==null){
			throw new RuntimeException("订单不存在");
		}
		if (order.getId().longValue()!=orderId.longValue()){
			throw new RuntimeException("订单不相符");
		}
		order.setTransactionId(transactionId);//交易流水号
		order.setPayTime(new Date());
		order.setStatus("1");//支付成功
		seckillOrderMapper.insert(order);//保存订单到数据库
		redisTemplate.boundHashOps("seckillOrder").delete(userId);//清除缓存
		System.out.println("支付成功后清除秒杀订单信息");
	}

	@Override
	public void deleteOrderFromRedis(String userId, Long orderId) {
		TbSeckillOrder seckillOrder=(TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
		if (seckillOrder!=null && seckillOrder.getId().longValue()==orderId.longValue()){
			redisTemplate.boundHashOps("seckillOrder").delete(userId);
			//从缓存中提取秒杀商品
			TbSeckillGoods seckillGoods =(TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(seckillOrder.getSeckillId());
			if (seckillGoods!=null){
				seckillGoods.setStockCount(seckillGoods.getStockCount()+1);//恢复库存
				redisTemplate.boundHashOps("seckillGoods").put(seckillGoods.getId(),seckillGoods);//存入缓存
				System.out.println("超时付款,恢复秒杀缓存商品库存");
			}

		}
	}

}
