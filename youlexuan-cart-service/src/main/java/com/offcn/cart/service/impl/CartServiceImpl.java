package com.offcn.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.offcn.cart.service.CartService;
import com.offcn.entity.Cart;
import com.offcn.mapper.TbItemMapper;
import com.offcn.pojo.TbItem;
import com.offcn.pojo.TbOrderItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private TbItemMapper itemMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 添加商品到购物车
     * @param cartList
     * @param itemId
     * @param num
     * @return
     */
    @Override
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {
        // 根据skuid查询商品对象
        TbItem item = itemMapper.selectByPrimaryKey(itemId);
        if (item==null){
            throw new RuntimeException("商品不存在");
        }
        if (!item.getStatus().equals("1")){
            throw new RuntimeException("该商品无效");
        }
        String sellerId = item.getSellerId();//获取商家id
        System.out.println("商家id-----"+sellerId);


        //根据商家id判断购物车是否存在该商家的购物车项
        Cart cart = searchCartBySellerId(cartList, sellerId);
        if (cart==null){
            cart =new Cart();//新建购物车对象
            cart.setSellerId(sellerId);
            cart.setSellerName(item.getSeller());
            TbOrderItem orderItem = createOrderItem(item, num);//创建订单明细
            List orderItemList=new ArrayList<>();
            orderItemList.add(orderItem);
            cart.setOrderItemList(orderItemList);

            cartList.add(cart);//添加到购物车列表

        }else {//如果该商家购物项已存在购物车列表

            //判断购物项有没有存在该商品
            TbOrderItem orderItem = searchOrderItemByItemId(cart.getOrderItemList(), itemId);
            if (orderItem==null){
                //需要新添加购物车明细
                TbOrderItem tbOrderItem=createOrderItem(item,num);
                cart.getOrderItemList().add(tbOrderItem);

            }else {
                //如果存在就改变商品数量和金额
                orderItem.setNum(orderItem.getNum()+num);
                orderItem.setTotalFee(new BigDecimal(orderItem.getNum()*orderItem.getPrice().doubleValue()));
                if (orderItem.getNum()<=0){
                    //如果商品小于等于0则移除该商品
                    cart.getOrderItemList().remove(orderItem);
                }
                //如果该店铺的购物车为0，则移除该购物车
                if (cart.getOrderItemList().size()<=0){
                    cartList.remove(cart);
                }
            }

        }


        return cartList;
    }

    /**
     * 从缓存中读取购物车列表
     * @param username
     * @return
     */
    @Override
    public List<Cart> findCartListFromRedis(String username) {
        System.out.println("从redis中读取购物车列表"+"当前用户"+username);
        List<Cart> cartList =(List<Cart>) redisTemplate.boundHashOps("cartList").get(username);
        if (cartList==null){
            cartList=new ArrayList<>();
        }
        return cartList;
    }

    /**
     * 保存购物车列表到redis缓存中
     * @param username
     * @param cartList
     */
    @Override
    public void saveCartListToRedis(String username, List<Cart> cartList) {
        System.out.println("向redis存储数据------");
        redisTemplate.boundHashOps("cartList").put(username,cartList);

    }

    /**
     * 合并购物车 登陆后将cookie中的数据存入redis，清空cookie
     * @param cartList1
     * @param cartList2
     * @return
     */
    @Override
    public List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2) {
        for (Cart cart : cartList2) {
            for (TbOrderItem orderItem : cart.getOrderItemList()) {
                cartList1 = addGoodsToCartList(cartList1, orderItem.getItemId(), orderItem.getNum());
            }
        }
        return cartList1;
    }

    //根据商家id判断购物车(项)集合是否存在该商家的购物车对象
    private Cart searchCartBySellerId(List<Cart> cartList,String sellerId){
        for (Cart cart : cartList) {
            if (cart.getSellerId().equals(sellerId)){
                return cart;
            }
        }
        return null;

    }

    //根据商品明细id查询
    private TbOrderItem searchOrderItemByItemId(List<TbOrderItem> orderItemList,Long itemId){
        for (TbOrderItem orderItem : orderItemList) {
            if (orderItem.getItemId().longValue()==itemId.longValue()){
                return orderItem;
            }
        }
        return null;
    }

    // 创建订单明细
    private TbOrderItem createOrderItem(TbItem item,Integer num){
        TbOrderItem orderItem=new TbOrderItem();
        orderItem.setGoodsId(item.getGoodsId());
        orderItem.setItemId(item.getId());
        orderItem.setNum(num);
        orderItem.setPicPath(item.getImage());
        orderItem.setPrice(item.getPrice());
        orderItem.setTitle(item.getTitle());
        orderItem.setTotalFee(new BigDecimal(num*item.getPrice().doubleValue()));
        return orderItem;

    }
}
