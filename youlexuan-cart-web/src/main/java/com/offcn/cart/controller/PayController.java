package com.offcn.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.offcn.entity.Result;
import com.offcn.order.service.OrderService;
import com.offcn.pay.service.AliPayService;
import com.offcn.pojo.TbPayLog;
import com.offcn.utils.IdWorker;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/pay")
public class PayController {

    @Reference
    private AliPayService aliPayService;
    @Reference
    private OrderService orderService;

    @RequestMapping("/createNative")
    public Map createNative(){
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("userId:"+userId);
        TbPayLog payLog = orderService.searchPayLogFromRedis(userId);
        System.out.println("2-2:"+payLog.getOutTradeNo());
        IdWorker idWorker = new IdWorker();
        if (payLog!=null){
            return aliPayService.createNative(payLog.getOutTradeNo() + "", payLog.getTotalFee()+"");
        }else {
            return new HashMap();
        }

    }

    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(String out_trade_no){
        Result result=null;
        int x=0;

        while (true){
            Map<String,String> map=null;
            try {
                map = aliPayService.queryPayStatus(out_trade_no);
            } catch (Exception e) {
                System.out.println("调用查询服务出错");
            }
            if (map==null){
                result=new Result(false,"支付出错");
                break;
            }
            if (map.get("tradestatus")!=null && map.get("tradestatus").equals("TRADE_SUCCESS")){
                result=new Result(true,"支付成功");
                //支付成功修改订单状态
                System.out.println("订单号2-2："+out_trade_no);
                orderService.updateOrderStatus(out_trade_no,map.get("trade_no"));

                break;
            }
            if (map.get("tradestatus")!=null && map.get("tradestatus").equals("TRADE_CLOSED")){
                result=new  Result(true, "未付款交易超时关闭，或支付完成后全额退款");
                break;
            }
            if (map.get("tradestatus")!=null&&map.get("tradestatus").equals("TRADE_FINISHED")){
                result=new  Result(true, "交易结束，不可退款");
                break;
            }
            try {
                Thread.sleep(3000);//每隔3秒循环一次
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            x++;
            if (x>=100){
                result=new Result(false,"二维码失效");
                break;
            }

        }
        return result;

    }
}
