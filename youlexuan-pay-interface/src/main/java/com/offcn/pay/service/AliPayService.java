package com.offcn.pay.service;

import java.util.Map;

public interface AliPayService {


    /**
     * 生成二维码
     * @param out_trade_no 订单号
     * @param total_fee 金额 分
     * @return
     */
    public Map createNative(String out_trade_no,String total_fee);


    /**
     * 查询订单状态
     * @param out_trade_no
     * @return
     */
    public Map queryPayStatus(String out_trade_no);

    /**
     * 关闭支付宝订单
     */
    public Map closePay(String out_trade_no);
}
