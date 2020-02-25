package com.offcn.cart.service;

import com.offcn.entity.Cart;

import java.util.List;

public interface CartService {

    /**
     * 添加商品到购物车
     */
    public List<Cart> addGoodsToCartList(List<Cart> cartList,Long itemId,Integer num);


    /**
     * 从redis中查询购物车列表
     */
    public List<Cart> findCartListFromRedis(String username);

    /**
     * 添加购物车到redis缓存中
     */
    public void saveCartListToRedis(String username,List<Cart> cartList);

    /**
     * 合并购物车 登陆后将cookie中的数据存入redis，清空cookie
     */
    public List<Cart> mergeCartList(List<Cart> cartList1,List<Cart> cartList2);
}
