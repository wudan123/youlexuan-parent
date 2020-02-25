package com.offcn.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.offcn.entity.Cart;
import com.offcn.entity.PageResult;
import com.offcn.mapper.TbOrderItemMapper;
import com.offcn.mapper.TbOrderMapper;
import com.offcn.mapper.TbPayLogMapper;
import com.offcn.order.service.OrderService;
import com.offcn.pojo.TbOrder;
import com.offcn.pojo.TbOrderExample;
import com.offcn.pojo.TbOrderExample.Criteria;
import com.offcn.pojo.TbOrderItem;
import com.offcn.pojo.TbPayLog;
import com.offcn.utils.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class OrderServiceImpl implements OrderService {

	@Autowired
	private TbOrderMapper orderMapper;
	@Autowired
	private RedisTemplate redisTemplate;
	@Autowired
	private TbOrderItemMapper orderItemMapper;
	@Autowired
	private IdWorker idWorker;
	@Autowired
	private TbPayLogMapper payLogMapper;
	/**
	 * 查询全部
	 */
	@Override
	public List<TbOrder> findAll() {
		return orderMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbOrder> page=   (Page<TbOrder>) orderMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbOrder order) {
		//从缓存中获取购物车列表数据
		List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(order.getUserId());
		List<String> orderIdList=new ArrayList<>();//订单id列表
		double total_money=0.0;//总金额

		for (Cart cart : cartList) {
			long orderId = idWorker.nextId();
			System.out.println("orderId-------"+orderId);
			System.out.println("sellerId-----"+cart.getSellerId());
			TbOrder tbOrder=new TbOrder();//创建订单对象
			tbOrder.setOrderId(orderId);
			System.out.println("======"+tbOrder.getOrderId());
			tbOrder.setUserId(order.getUserId());//用户id
			tbOrder.setCreateTime(new Date());
			tbOrder.setPaymentType("1");//在线支付
			tbOrder.setReceiver(order.getReceiver());//收件人
			tbOrder.setReceiverMobile(order.getReceiverMobile());
			tbOrder.setReceiverAreaName(order.getReceiverAreaName());
			tbOrder.setSellerId(cart.getSellerId());//商家id
			tbOrder.setSourceType(order.getSourceType());//订单来源 app pc网站
			tbOrder.setStatus("1");//状态：1、未付款，2、已付款，3、未发货，4、已发货

			double money=0.0;
			for (TbOrderItem orderItem : cart.getOrderItemList()) {//订单明细
				orderItem.setId(idWorker.nextId());
				orderItem.setOrderId(orderId);
				orderItem.setSellerId(cart.getSellerId());
				money+=orderItem.getTotalFee().doubleValue();//金额累加
				orderItemMapper.insert(orderItem);
			}
			tbOrder.setPayment(new BigDecimal(money));//订单支付金额
			orderMapper.insert(tbOrder);
			System.out.println("订单生成成功!");
			orderIdList.add(orderId+"");//订单id加入到支付日志
			total_money+=money;//总金额累加

		}
		if (order.getPaymentType().equals("1")){
			//如果是支付宝支付
			TbPayLog payLog=new TbPayLog();
			String outTradeNo=idWorker.nextId()+"";
			payLog.setOutTradeNo(outTradeNo);
			payLog.setCreateTime(new Date());
			String ids = orderIdList.toString().replace("[", "").replace("]", "");
			payLog.setOrderList(ids);
			payLog.setPayType("1");
			System.out.println("总金额(分):"+total_money);
			BigDecimal total_money1 = BigDecimal.valueOf(total_money);
			BigDecimal cj = BigDecimal.valueOf(100d);
			//高精度乘法
			BigDecimal bigDecimal = total_money1.multiply(cj);
			double hj=total_money*100;
			System.out.println("合计(元):"+hj);
			System.out.println("高精度处理:"+bigDecimal.toBigInteger().longValue());
			payLog.setTotalFee(bigDecimal.toBigInteger().longValue());//支付日志总金额
			payLog.setTradeState("0");
			payLog.setUserId(order.getUserId());
			System.out.println("-----------"+payLog.toString());
			payLogMapper.insert(payLog);//添加支付日志
			redisTemplate.boundHashOps("payLog").put(order.getUserId(),payLog);//支付日志加入缓存
			System.out.println("支付日志加入缓存成功!2-2:"+payLog.getOutTradeNo());
		}
		redisTemplate.boundHashOps("cartList").delete(order.getUserId());//清除购物车缓存
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbOrder order){
		orderMapper.updateByPrimaryKey(order);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param
	 * @return
	 */
	@Override
	public TbOrder findOne(Long orderId){
		return orderMapper.selectByPrimaryKey(orderId);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] orderIds) {
		for(Long orderId:orderIds){
			orderMapper.deleteByPrimaryKey(orderId);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbOrder order, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbOrderExample example=new TbOrderExample();
		Criteria criteria = example.createCriteria();
		
		if(order!=null){			
						if(order.getPaymentType()!=null && order.getPaymentType().length()>0){
				criteria.andPaymentTypeLike("%"+order.getPaymentType()+"%");
			}			if(order.getPostFee()!=null && order.getPostFee().length()>0){
				criteria.andPostFeeLike("%"+order.getPostFee()+"%");
			}			if(order.getStatus()!=null && order.getStatus().length()>0){
				criteria.andStatusLike("%"+order.getStatus()+"%");
			}			if(order.getShippingName()!=null && order.getShippingName().length()>0){
				criteria.andShippingNameLike("%"+order.getShippingName()+"%");
			}			if(order.getShippingCode()!=null && order.getShippingCode().length()>0){
				criteria.andShippingCodeLike("%"+order.getShippingCode()+"%");
			}			if(order.getUserId()!=null && order.getUserId().length()>0){
				criteria.andUserIdLike("%"+order.getUserId()+"%");
			}			if(order.getBuyerMessage()!=null && order.getBuyerMessage().length()>0){
				criteria.andBuyerMessageLike("%"+order.getBuyerMessage()+"%");
			}			if(order.getBuyerNick()!=null && order.getBuyerNick().length()>0){
				criteria.andBuyerNickLike("%"+order.getBuyerNick()+"%");
			}			if(order.getBuyerRate()!=null && order.getBuyerRate().length()>0){
				criteria.andBuyerRateLike("%"+order.getBuyerRate()+"%");
			}			if(order.getReceiverAreaName()!=null && order.getReceiverAreaName().length()>0){
				criteria.andReceiverAreaNameLike("%"+order.getReceiverAreaName()+"%");
			}			if(order.getReceiverMobile()!=null && order.getReceiverMobile().length()>0){
				criteria.andReceiverMobileLike("%"+order.getReceiverMobile()+"%");
			}			if(order.getReceiverZipCode()!=null && order.getReceiverZipCode().length()>0){
				criteria.andReceiverZipCodeLike("%"+order.getReceiverZipCode()+"%");
			}			if(order.getReceiver()!=null && order.getReceiver().length()>0){
				criteria.andReceiverLike("%"+order.getReceiver()+"%");
			}			if(order.getInvoiceType()!=null && order.getInvoiceType().length()>0){
				criteria.andInvoiceTypeLike("%"+order.getInvoiceType()+"%");
			}			if(order.getSourceType()!=null && order.getSourceType().length()>0){
				criteria.andSourceTypeLike("%"+order.getSourceType()+"%");
			}			if(order.getSellerId()!=null && order.getSellerId().length()>0){
				criteria.andSellerIdLike("%"+order.getSellerId()+"%");
			}	
		}
		
		Page<TbOrder> page= (Page<TbOrder>)orderMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public TbPayLog searchPayLogFromRedis(String userId) {
		TbPayLog payLog = (TbPayLog) redisTemplate.boundHashOps("payLog").get(userId);
		if (payLog!=null){
			System.out.println("======="+payLog);

			return payLog;
		}
		return null;

	}

	@Override
	public void updateOrderStatus(String out_trade_no, String transaction_id) {
		//修改支付日志状态
		System.out.println("1-1:"+out_trade_no);
		TbPayLog payLog = payLogMapper.selectByPrimaryKey(out_trade_no);
		System.out.println("支付日志："+payLog.toString());
		payLog.setPayTime(new Date());
		payLog.setTradeState("1");
		payLog.setTransactionId(transaction_id);
		payLogMapper.updateByPrimaryKey(payLog);

		//修改订单状态
		String orderList = payLog.getOrderList();
		String[] ids = orderList.split(",");//订单id
		for (String id : ids) {
			TbOrder order = orderMapper.selectByPrimaryKey(Long.parseLong(id));
			if (order!=null){
				order.setStatus("2");//已付款
				orderMapper.updateByPrimaryKey(order);
			}

		}
		redisTemplate.boundHashOps("payLog").delete(payLog.getUserId());// 清除支付日志缓存
		System.out.println("清除支付日志缓存成功");


	}

}
