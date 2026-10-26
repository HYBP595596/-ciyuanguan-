package com.dimension.payment;

import java.math.BigDecimal;

/** 预下单请求 */
public class PayRequest {
    private String paymentNo;   // 平台支付单号
    private String outTradeNo;  // 业务订单号
    private BigDecimal amount;  // 金额
    private String subject;     // 商品标题
    private String notifyUrl;   // 异步通知地址
    private String returnUrl;   // 同步跳转地址

    public String getPaymentNo() { return paymentNo; }
    public void setPaymentNo(String paymentNo) { this.paymentNo = paymentNo; }
    public String getOutTradeNo() { return outTradeNo; }
    public void setOutTradeNo(String outTradeNo) { this.outTradeNo = outTradeNo; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getNotifyUrl() { return notifyUrl; }
    public void setNotifyUrl(String notifyUrl) { this.notifyUrl = notifyUrl; }
    public String getReturnUrl() { return returnUrl; }
    public void setReturnUrl(String returnUrl) { this.returnUrl = returnUrl; }
}
