package com.offcn.seckill.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.offcn.entity.Result;
import com.offcn.pay.service.AliPayService;
import com.offcn.pojo.TbSeckillOrder;
import com.offcn.seckill.service.SeckillOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/pay")
public class PayController {

    @Reference
    AliPayService aliPayService;
    @Reference
    private SeckillOrderService seckillOrderService;

    @RequestMapping("/createNative")
    public Map createNative(){
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        TbSeckillOrder seckillOrder = seckillOrderService.searchOrderFromRedisByUserId(userId);
        if (seckillOrder==null){
            return new HashMap();
        }else {
            long fen = (long) (seckillOrder.getMoney().doubleValue()*100);//金额单位转化为分
            return aliPayService.createNative(seckillOrder.getId() + "", fen + "");
        }
    }
    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(String out_trade_no){
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        Result result=null;
        int x=0;
        while (true){
            Map<String,String> map = aliPayService.queryPayStatus(out_trade_no);

            if (map==null){
                result=new Result(false,"支付出错");
                break;
            }
            if (map.get("tradestatus")!=null &&map.get("tradestatus").equals("TRADE_SUCCESS")){
                result=new Result(true,"支付成功");
                //支付成功后保存订单信息到数据库
                seckillOrderService.saveOrderFromRedisToDb(userId,Long.valueOf(out_trade_no),map.get("trade_no"));
                break;
            }
            if (map.get("tradestatus")!=null &&map.get("tradestatus").equals("TRADE_CLOSED")) {
                result = new Result(true, "未付款交易超时");
                break;
            }
            if (map.get("tradestatus")!=null&&map.get("tradestatus").equals("TRADE_FINISHED")){
                result=new Result(true,"交易结束");
                break;
            }
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (x>=100){

                result=new Result(false,"二维码超时");
                //调用二维码关闭接口
                Map closePayMap = aliPayService.closePay(out_trade_no);
                if (closePayMap.get("code").equals("10000")){
                    System.out.println("支付超时，取消订单");
                    seckillOrderService.deleteOrderFromRedis(userId,Long.valueOf(out_trade_no));
                }
                break;
            }
        }
        return result;

    }
}
