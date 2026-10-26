package com.dimension.service;

import com.dimension.entity.Payment;
import com.dimension.payment.NotifyResult;

import java.util.Map;

public interface PaymentService {

    /** 预下单：为业务订单创建平台支付单 */
    Payment createPayment(Long buyerId, String orderNo, String channel);

    /** 生成给前端的支付参数（二维码/收银台地址等） */
    Map<String, Object> payParams(Payment payment);

    /** 统一异步通知处理：验签 → 加锁查单 → 金额核对 → 幂等 → 同事务驱动订单 */
    NotifyResult handleNotify(String channel, Map<String, String> params);

    Payment query(String paymentNo);

    /** 按业务订单号查最新支付单 */
    Payment queryByOutTradeNo(String outTradeNo);

    /** 退款（买家自助：仅已支付且未交付的订单） */
    boolean refund(Long buyerId, String paymentNo, String reason);

    /** 管理端强制退款：按业务订单号找已支付单，允许「待验收」状态 */
    boolean refundByOrder(String orderNo, String reason, Long adminId);

    /** 模拟渠道：生成一份带签名的回调参数，供收银台页面回传 */
    Map<String, String> buildMockCashier(Payment payment);
}
