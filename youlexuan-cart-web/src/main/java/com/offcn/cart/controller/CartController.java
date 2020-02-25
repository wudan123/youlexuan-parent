package com.offcn.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.offcn.cart.service.CartService;
import com.offcn.entity.Cart;
import com.offcn.entity.Result;
import com.offcn.pojo.TbItem;
import com.offcn.utils.CookieUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Reference(timeout = 6000)
    private CartService cartService;
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private HttpServletResponse response;


    /**
     * 查询购物车列表
     * @return
     */
    @RequestMapping("/findCartList")
    public List<Cart> findCartList(){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("当前登陆人用户:"+username);
        String cartListStr = CookieUtil.getCookieValue(request, "cartList", "UTF-8");
        if (cartListStr==null || cartListStr.equals("")){//如果cookie为空
            cartListStr="[]";
        }
        List<Cart> cartList_cookie=null;
        try {
            //将字符串转化成Cart对象数组
            cartList_cookie = JSON.parseArray(cartListStr, Cart.class);//cookie中的购物车列表
        } catch (Exception e) {
            e.printStackTrace();
            cartList_cookie=new ArrayList<>();
        }
        if (username.equals("anonymousUser")){//如果未登陆,读取cookie

            return cartList_cookie;
        }else {
            //已登陆则读取redis缓存
            List<Cart> cartListFromRedis = cartService.findCartListFromRedis(username);
            if (cartList_cookie.size()>0){
                //如果cookie中有数据,合并购物车
                cartListFromRedis = cartService.mergeCartList(cartListFromRedis, cartList_cookie);
                //清楚本地cookie数据
                CookieUtil.deleteCookie(request,response,"cartList");
                //将合并后的数据存入redis缓存
                cartService.saveCartListToRedis(username,cartListFromRedis);
                System.out.println("合并购物车!");

            }
            return cartListFromRedis;
        }



    }


    /**
     * 添加购物车
     */
    @RequestMapping("/addCartList")
    public Result addCartList(Long itemId,Integer num){
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            System.out.println("当前登陆人用户:"+username);

            //设置响应头  允许跨域请求
            response.setHeader("Access-Control-Allow-Origin", "http://localhost:9106");
            response.setHeader("Access-Control-Allow-Credentials", "true");

            List<Cart> cartList = this.findCartList();//获取购物车列表
            cartList = cartService.addGoodsToCartList(cartList, itemId, num);
            if (username.equals("anonymousUser")){//未登陆
                CookieUtil.setCookie(request,response,"cartList",JSON.toJSONString(cartList),3600*24,"UTF-8");
                System.out.println("购物车添加到Cookie成功---");
            }else {
                cartService.saveCartListToRedis(username,cartList);
                System.out.println("购物车添加到redis!!!");
            }

            return new Result(true,"添加成功!");

        } catch (RuntimeException e) {
            e.printStackTrace();
            return new Result(false,e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"添加失败!");
        }


    }




}
