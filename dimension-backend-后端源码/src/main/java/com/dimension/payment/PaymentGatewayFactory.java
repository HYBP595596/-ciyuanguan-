package com.dimension.payment;

import java.util.HashMap;
import java.util.Map;

/**
 * 网关工厂：按渠道取实现。
 * 新增渠道只要实现 PaymentGateway 并在静态块里 register(...)，业务代码调用方式不变。
 *
 * 已注册：
 *   MOCK   本地模拟渠道（默认，开发/演示用）
 *   WECHAT 微信支付 V2 协议适配（需在 payment.properties 配商户参数，未配置则 isConfigured=false）
 *   ALIPAY 支付宝 RSA2 协议适配（同上）
 */
public final class PaymentGatewayFactory {

    private static final Map<String, PaymentGateway> REGISTRY = new HashMap<>();

    static {
        register(new MockPaymentGateway());
        register(new WechatGateway());
        register(new AlipayGateway());
    }

    private PaymentGatewayFactory() {
    }

    public static void register(PaymentGateway gateway) {
        if (gateway != null && gateway.channel() != null) {
            REGISTRY.put(gateway.channel().toUpperCase(), gateway);
        }
    }

    public static PaymentGateway of(String channel) {
        String key = (channel == null || channel.trim().isEmpty())
                ? MockPaymentGateway.CHANNEL : channel.trim().toUpperCase();
        return REGISTRY.get(key);
    }

    public static boolean supports(String channel) {
        return of(channel) != null;
    }

    /** 渠道可用 = 已注册 且 参数已配置 */
    public static boolean usable(String channel) {
        PaymentGateway gateway = of(channel);
        return gateway != null && gateway.isConfigured();
    }

    /** 给前端展示：当前平台可用的支付渠道 */
    public static Map<String, Boolean> availability() {
        Map<String, Boolean> map = new HashMap<>();
        for (Map.Entry<String, PaymentGateway> e : REGISTRY.entrySet()) {
            map.put(e.getKey(), e.getValue().isConfigured());
        }
        return map;
    }
}
