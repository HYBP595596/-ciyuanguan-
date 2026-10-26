package com.dimension.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 平台支付单 dimension_payment
 */
@Data
public class Payment {
    private Long id;
    /** 平台支付单号 */
    private String paymentNo;
    /** 业务订单号 */
    private String outTradeNo;
    private Long buyerId;
    /** 支付渠道：MOCK / WECHAT / ALIPAY */
    private String channel;
    private BigDecimal amount;
    private String subject;
    /** 0待支付 1已支付 2已关闭 3已退款 */
    private Integer status;
    /** 三方交易号 */
    private String tradeNo;
    private String notifyUrl;
    private Date paidAt;
    private Date closeAt;
    private String rawNotify;
    private Date createTime;
    private Date updateTime;
}
