package com.dimension.payment;

import java.util.HashMap;
import java.util.Map;

/**
 * 网关工厂：按渠道取实现。
 * 以后接微信/支付宝，只需在静态块里 register(new WechatGateway())，
 * 业务代码调用方式不变。
 */
public final class PaymentGatewayFactory {

    private static final Map<String, PaymentGateway> REGISTRY = new HashMap<>();

    static {
        register(new MockPaymentGateway());
        // register(new WechatGateway());
        // register(new AlipayGateway());
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
}
