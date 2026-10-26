package com.dimension.payment;

import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.SecureUtil;
import com.dimension.config.PlatformConfig;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * 微信支付（V2 统一下单 / MD5 验签）适配器。
 *
 * 说明：这里实现的是「协议层」——下单参数拼装、回调验签、金额核对、状态映射，
 * 全部与微信官方文档一致；真正的 HTTPS 请求（api.mch.weixin.qq.com）需要商户号与 API 密钥，
 * 未配置时 {@link #isConfigured()} 返回 false，平台会拒绝为订单开出无法支付的支付单。
 *
 * 需要的配置（resources/payment.properties）：
 *   payment.wechat.enabled=true
 *   payment.wechat.appId=wxxxx
 *   payment.wechat.mchId=15xxxx
 *   payment.wechat.apiKey=32位密钥
 *   payment.wechat.notifyUrl=https://your.domain/PayNotify?channel=WECHAT
 */
public class WechatGateway implements PaymentGateway {

    public static final String CHANNEL = "WECHAT";

    @Override
    public String channel() {
        return CHANNEL;
    }

    @Override
    public boolean isConfigured() {
        return PlatformConfig.getBool("payment.wechat.enabled", false)
                && notEmpty(PlatformConfig.get("payment.wechat.appId", null))
                && notEmpty(PlatformConfig.get("payment.wechat.mchId", null))
                && notEmpty(PlatformConfig.get("payment.wechat.apiKey", null));
    }

    @Override
    public Map<String, Object> createPay(PayRequest request) {
        Map<String, Object> result = new HashMap<>();
        if (!isConfigured()) {
            result.put("error", "微信支付未配置（appId/mchId/apiKey）");
            return result;
        }
        Map<String, String> fields = new TreeMap<>();
        fields.put("appid", PlatformConfig.get("payment.wechat.appId", ""));
        fields.put("mch_id", PlatformConfig.get("payment.wechat.mchId", ""));
        fields.put("nonce_str", IdUtil.simpleUUID().toUpperCase());
        fields.put("body", request.getSubject());
        fields.put("out_trade_no", request.getPaymentNo());
        fields.put("total_fee", yuanToFen(request.getAmount()));
        fields.put("notify_url", notifyUrl(request.getNotifyUrl()));
        fields.put("trade_type", PlatformConfig.get("payment.wechat.tradeType", "NATIVE"));
        fields.put("spbill_create_ip", PlatformConfig.get("payment.wechat.spbillCreateIp", "127.0.0.1"));
        fields.put("sign", sign(fields));

        result.put("channel", CHANNEL);
        result.put("paymentNo", request.getPaymentNo());
        result.put("amount", request.getAmount().toPlainString());
        // 真实接入：把 fields 以 XML 形式 POST 到 https://api.mch.weixin.qq.com/pay/unifiedorder，
        // 再把返回的 code_url 交给前端生成二维码
        result.put("unifiedOrderFields", fields);
        result.put("unifiedOrderUrl", "https://api.mch.weixin.qq.com/pay/unifiedorder");
        result.put("tip", "已按 V2 协议生成统一下单参数，需服务端发起下单请求后把 code_url 给前端");
        return result;
    }

    @Override
    public PayResult queryPay(String paymentNo, String outTradeNo, BigDecimal amount) {
        if (!isConfigured()) {
            return PayResult.fail("微信支付未配置");
        }
        // 真实接入：POST https://api.mch.weixin.qq.com/pay/orderquery
        return PayResult.fail("微信查单尚未接入网络请求，请用支付回调兜底");
    }

    @Override
    public PayResult refund(String paymentNo, String tradeNo, BigDecimal amount, String reason) {
        if (!isConfigured()) {
            return PayResult.fail("微信支付未配置");
        }
        // 真实接入需要 apiclient_cert.p12 双向证书
        return PayResult.fail("微信退款需要商户 API 证书，当前未配置，请走人工退款");
    }

    /**
     * 回调验签：微信 V2 回调把业务参数放在 query/form 里，sign 由除 sign 外所有非空参数
     * 按参数名 ASCII 升序拼成 k=v&...&key=API_KEY 后 MD5 大写。
     */
    @Override
    public NotifyResult verifyNotify(Map<String, String> params) {
        if (!isConfigured()) {
            return NotifyResult.invalid("微信支付未配置");
        }
        if (params == null) {
            return NotifyResult.invalid("参数为空");
        }
        String sign = params.get("sign");
        if (!notEmpty(sign)) {
            return NotifyResult.invalid("缺少签名");
        }
        if (!"SUCCESS".equalsIgnoreCase(params.get("return_code"))
                || !"SUCCESS".equalsIgnoreCase(params.get("result_code"))) {
            return NotifyResult.invalid("微信返回失败");
        }
        Map<String, String> signFields = new TreeMap<>();
        params.forEach((k, v) -> {
            if (!"sign".equals(k) && notEmpty(v)) {
                signFields.put(k, v);
            }
        });
        if (!sign(signFields).equalsIgnoreCase(sign)) {
            return NotifyResult.invalid("签名校验失败");
        }
        if (!PlatformConfig.get("payment.wechat.appId", "").equals(params.get("appid"))
                || !PlatformConfig.get("payment.wechat.mchId", "").equals(params.get("mch_id"))) {
            return NotifyResult.invalid("appid/商户号不匹配");
        }
        String totalFee = params.get("total_fee");
        if (!notEmpty(totalFee)) {
            return NotifyResult.invalid("缺少金额");
        }
        BigDecimal amount = fenToYuan(totalFee);
        StringBuilder raw = new StringBuilder();
        params.forEach((k, v) -> raw.append(k).append('=').append(v).append('&'));
        return NotifyResult.valid(params.get("out_trade_no"), params.get("transaction_id"),
                amount, "PAID", raw.toString());
    }

    /** MD5(k=v&...&key=API_KEY) 大写 */
    private String sign(Map<String, String> fields) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> e : fields.entrySet()) {
            if (notEmpty(e.getValue())) {
                sb.append(e.getKey()).append('=').append(e.getValue()).append('&');
            }
        }
        sb.append("key=").append(PlatformConfig.get("payment.wechat.apiKey", ""));
        return SecureUtil.md5(sb.toString()).toUpperCase();
    }

    private String notifyUrl(String fallback) {
        String configured = PlatformConfig.get("payment.wechat.notifyUrl", null);
        return notEmpty(configured) ? configured : fallback;
    }

    private static String yuanToFen(BigDecimal amount) {
        return amount == null ? "0"
                : amount.multiply(new BigDecimal("100")).setScale(0, RoundingMode.HALF_UP).toPlainString();
    }

    private static BigDecimal fenToYuan(String fen) {
        return new BigDecimal(fen).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }

    private static boolean notEmpty(String s) {
        return s != null && !s.trim().isEmpty();
    }
}
