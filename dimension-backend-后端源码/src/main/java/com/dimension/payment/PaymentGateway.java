package com.dimension.payment;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 第三方支付网关标准接口。
 * 平台不直接依赖某个支付公司的 SDK，而是依赖这层抽象；
 * 真实接入时新增 WechatGateway / AlipayGateway 实现即可，业务代码不用改。
 */
public interface PaymentGateway {

    /** 渠道标识：MOCK / WECHAT / ALIPAY */
    String channel();

    /**
     * 预下单（统一下单）。返回给前端的支付参数，
     * 真实渠道会返回 code_url / 跳转链接 / prepay_id 等。
     */
    Map<String, Object> createPay(PayRequest request);

    /** 主动查单（用于前端轮询、掉单补偿） */
    PayResult queryPay(String paymentNo, String outTradeNo, BigDecimal amount);

    /** 退款 */
    PayResult refund(String paymentNo, String tradeNo, BigDecimal amount, String reason);

    /**
     * 异步通知验签 + 解析。
     * 返回 valid=false 表示签名不合法或参数缺失，绝不能再往下驱动业务。
     */
    NotifyResult verifyNotify(Map<String, String> params);
}
