package com.dimension.payment;

import cn.hutool.crypto.SecureUtil;
import com.dimension.config.PlatformConfig;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 模拟支付网关：接口形态与真实渠道完全一致，只有"网络连接"是本地模拟的。
 *
 * 契约（与真实渠道对齐）：
 *   1. 预下单返回支付参数（这里是收银台地址 + 待签字段）
 *   2. 用户在收银台完成支付后，渠道以【回调】方式通知平台
 *   3. 回调必须携带 sign，平台用渠道密钥验签
 *   4. 平台只信任验签通过且金额一致的回调
 */
public class MockPaymentGateway implements PaymentGateway {

    public static final String CHANNEL = "MOCK";

    @Override
    public String channel() {
        return CHANNEL;
    }

    @Override
    public Map<String, Object> createPay(PayRequest request) {
        Map<String, Object> params = new HashMap<>();
        params.put("channel", CHANNEL);
        params.put("paymentNo", request.getPaymentNo());
        params.put("outTradeNo", request.getOutTradeNo());
        params.put("amount", request.getAmount().toPlainString());
        params.put("subject", request.getSubject());
        // 真实渠道这里返回 code_url / prepay_id；模拟渠道返回本地收银台地址
        params.put("cashierUrl", apiBase() + "/Payment?action=mockCashier&paymentNo=" + request.getPaymentNo());
        return params;
    }

    @Override
    public PayResult queryPay(String paymentNo, String outTradeNo, BigDecimal amount) {
        // 模拟渠道的查单：由平台自身支付单状态决定，这里只做接口形状
        return PayResult.ok(null, "NOTPAY");
    }

    @Override
    public PayResult refund(String paymentNo, String tradeNo, BigDecimal amount, String reason) {
        // 模拟退款成功，返回渠道退款单号
        return PayResult.ok("MOCKREFUND" + System.currentTimeMillis(), "REFUND");
    }

    @Override
    public NotifyResult verifyNotify(Map<String, String> params) {
        if (params == null) {
            return NotifyResult.invalid("参数为空");
        }
        String paymentNo = trim(params.get("paymentNo"));
        String tradeNo = trim(params.get("tradeNo"));
        String amountStr = trim(params.get("amount"));
        String timestamp = trim(params.get("timestamp"));
        String sign = trim(params.get("sign"));

        if (paymentNo == null || tradeNo == null || amountStr == null || timestamp == null || sign == null) {
            return NotifyResult.invalid("回调参数缺失");
        }
        // 回调时效性（真实渠道一般要求 5~10 分钟内）
        try {
            long ts = Long.parseLong(timestamp);
            if (Math.abs(System.currentTimeMillis() - ts) > 10 * 60 * 1000L) {
                return NotifyResult.invalid("回调已过期");
            }
        } catch (NumberFormatException e) {
            return NotifyResult.invalid("时间戳不合法");
        }

        String expected = sign(paymentNo, amountStr, tradeNo, timestamp);
        if (!expected.equalsIgnoreCase(sign)) {
            return NotifyResult.invalid("签名校验失败");
        }

        BigDecimal amount;
        try {
            amount = new BigDecimal(amountStr);
        } catch (NumberFormatException e) {
            return NotifyResult.invalid("金额不合法");
        }

        StringBuilder raw = new StringBuilder();
        params.forEach((k, v) -> raw.append(k).append('=').append(v).append('&'));
        return NotifyResult.valid(paymentNo, tradeNo, amount, "PAID", raw.toString());
    }

    /**
     * 供"模拟收银台"使用：渠道在用户付款成功后生成带签名的回调参数。
     * 真实世界这一步发生在支付公司服务器上。
     */
    public Map<String, String> buildSignedNotify(String paymentNo, String amount) {
        String tradeNo = "MOCKTRADE" + System.currentTimeMillis();
        String timestamp = String.valueOf(System.currentTimeMillis());
        Map<String, String> params = new HashMap<>();
        params.put("paymentNo", paymentNo);
        params.put("tradeNo", tradeNo);
        params.put("amount", amount);
        params.put("timestamp", timestamp);
        params.put("sign", sign(paymentNo, amount, tradeNo, timestamp));
        return params;
    }

    /** HMAC-SHA256(paymentNo|amount|tradeNo|timestamp, secret) */
    private String sign(String paymentNo, String amount, String tradeNo, String timestamp) {
        String content = paymentNo + "|" + amount + "|" + tradeNo + "|" + timestamp;
        return SecureUtil.hmacSha256(secret()).digestHex(content);
    }

    private static String secret() {
        return PlatformConfig.get("payment.mock.secret", "dimension-mock-payment-secret");
    }

    private static String apiBase() {
        return PlatformConfig.apiBase();
    }

    private static String trim(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
