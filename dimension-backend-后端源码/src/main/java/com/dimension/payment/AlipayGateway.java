package com.dimension.payment;

import cn.hutool.core.date.DateUtil;
import com.dimension.config.PlatformConfig;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * 支付宝（开放平台 openapi.alipay.com）适配器。
 *
 * 实现的是「协议层」：page.pay 下单参数 + RSA2 签名、异步通知 RSA2 验签、金额与 app_id 核对。
 * 未配置 appId / 应用私钥 / 支付宝公钥时 {@link #isConfigured()} 为 false。
 *
 * 需要的配置（resources/payment.properties）：
 *   payment.alipay.enabled=true
 *   payment.alipay.appId=2021xxxx
 *   payment.alipay.privateKey=应用私钥(PKCS8, Base64)
 *   payment.alipay.publicKey=支付宝公钥(X509, Base64)
 *   payment.alipay.notifyUrl=https://your.domain/PayNotify?channel=ALIPAY
 */
public class AlipayGateway implements PaymentGateway {

    public static final String CHANNEL = "ALIPAY";

    @Override
    public String channel() {
        return CHANNEL;
    }

    @Override
    public boolean isConfigured() {
        return PlatformConfig.getBool("payment.alipay.enabled", false)
                && notEmpty(PlatformConfig.get("payment.alipay.appId", null))
                && notEmpty(PlatformConfig.get("payment.alipay.privateKey", null))
                && notEmpty(PlatformConfig.get("payment.alipay.publicKey", null));
    }

    /**
     * 电脑网站支付（alipay.trade.page.pay）：返回可以直接跳转的网关地址与已签名参数。
     */
    @Override
    public Map<String, Object> createPay(PayRequest request) {
        Map<String, Object> result = new HashMap<>();
        if (!isConfigured()) {
            result.put("error", "支付宝未配置（appId/应用私钥/支付宝公钥）");
            return result;
        }
        Map<String, String> biz = new TreeMap<>();
        biz.put("out_trade_no", request.getPaymentNo());
        biz.put("total_amount", request.getAmount().toPlainString());
        biz.put("subject", request.getSubject());
        biz.put("product_code", "FAST_INSTANT_TRADE_PAY");

        Map<String, String> params = new TreeMap<>();
        params.put("app_id", PlatformConfig.get("payment.alipay.appId", ""));
        params.put("method", "alipay.trade.page.pay");
        params.put("format", "JSON");
        params.put("charset", "utf-8");
        params.put("sign_type", "RSA2");
        params.put("timestamp", DateUtil.format(new java.util.Date(), "yyyy-MM-dd HH:mm:ss"));
        params.put("version", "1.0");
        params.put("notify_url", notifyUrl(request.getNotifyUrl()));
        params.put("biz_content", toJson(biz));
        params.put("sign", sign(params));

        result.put("channel", CHANNEL);
        result.put("paymentNo", request.getPaymentNo());
        result.put("amount", request.getAmount().toPlainString());
        result.put("gateway", PlatformConfig.get("payment.alipay.gateway",
                "https://openapi.alipay.com/gateway.do"));
        result.put("params", params);
        result.put("tip", "把 params 拼到 gateway? 后面即可跳转支付宝收银台");
        return result;
    }

    @Override
    public PayResult queryPay(String paymentNo, String outTradeNo, BigDecimal amount) {
        if (!isConfigured()) {
            return PayResult.fail("支付宝未配置");
        }
        return PayResult.fail("支付宝查单尚未接入网络请求，请用支付回调兜底");
    }

    @Override
    public PayResult refund(String paymentNo, String tradeNo, BigDecimal amount, String reason) {
        if (!isConfigured()) {
            return PayResult.fail("支付宝未配置");
        }
        return PayResult.fail("支付宝退款尚未接入网络请求，请走人工退款");
    }

    /**
     * 异步通知验签：除 sign / sign_type 外所有非空参数按参数名升序拼 k=v&... ，
     * 用支付宝公钥做 RSA2(SHA256WithRSA) 验签。
     */
    @Override
    public NotifyResult verifyNotify(Map<String, String> params) {
        if (!isConfigured()) {
            return NotifyResult.invalid("支付宝未配置");
        }
        if (params == null) {
            return NotifyResult.invalid("参数为空");
        }
        String sign = params.get("sign");
        if (!notEmpty(sign)) {
            return NotifyResult.invalid("缺少签名");
        }
        String tradeStatus = params.get("trade_status");
        if (!"TRADE_SUCCESS".equals(tradeStatus) && !"TRADE_FINISHED".equals(tradeStatus)) {
            return NotifyResult.invalid("交易未成功: " + tradeStatus);
        }
        Map<String, String> signFields = new TreeMap<>();
        params.forEach((k, v) -> {
            if (!"sign".equals(k) && !"sign_type".equals(k) && notEmpty(v)) {
                signFields.put(k, v);
            }
        });
        String content = join(signFields);
        if (!verifyRsa2(content, sign)) {
            return NotifyResult.invalid("签名校验失败");
        }
        if (!PlatformConfig.get("payment.alipay.appId", "").equals(params.get("app_id"))) {
            return NotifyResult.invalid("app_id 不匹配");
        }
        BigDecimal amount;
        try {
            amount = new BigDecimal(params.get("total_amount"));
        } catch (Exception e) {
            return NotifyResult.invalid("金额不合法");
        }
        StringBuilder raw = new StringBuilder();
        params.forEach((k, v) -> raw.append(k).append('=').append(v).append('&'));
        return NotifyResult.valid(params.get("out_trade_no"), params.get("trade_no"),
                amount, "PAID", raw.toString());
    }

    // ---------- 签名 / 验签 ----------

    private String sign(Map<String, String> params) {
        try {
            Signature signature = Signature.getInstance("SHA256WithRSA");
            signature.initSign(loadPrivateKey());
            signature.update(join(params).getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(signature.sign());
        } catch (Exception e) {
            return "";
        }
    }

    private boolean verifyRsa2(String content, String sign) {
        try {
            Signature signature = Signature.getInstance("SHA256WithRSA");
            signature.initVerify(loadPublicKey());
            signature.update(content.getBytes(StandardCharsets.UTF_8));
            return signature.verify(Base64.getDecoder().decode(sign));
        } catch (Exception e) {
            return false;
        }
    }

    private PrivateKey loadPrivateKey() throws Exception {
        byte[] key = Base64.getDecoder().decode(stripPem(PlatformConfig.get("payment.alipay.privateKey", "")));
        return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(key));
    }

    private PublicKey loadPublicKey() throws Exception {
        byte[] key = Base64.getDecoder().decode(stripPem(PlatformConfig.get("payment.alipay.publicKey", "")));
        return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(key));
    }

    private static String stripPem(String key) {
        return key.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
    }

    private static String join(Map<String, String> fields) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> e : fields.entrySet()) {
            if (notEmpty(e.getValue())) {
                if (sb.length() > 0) {
                    sb.append('&');
                }
                sb.append(e.getKey()).append('=').append(e.getValue());
            }
        }
        return sb.toString();
    }

    /** 只用来拼 biz_content，字段都是内部可控值，不需要完整 JSON 库 */
    private static String toJson(Map<String, String> fields) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, String> e : fields.entrySet()) {
            if (!first) {
                sb.append(',');
            }
            first = false;
            sb.append('"').append(e.getKey()).append("\":\"")
                    .append(e.getValue().replace("\"", "\\\""))
                    .append('"');
        }
        return sb.append('}').toString();
    }

    private String notifyUrl(String fallback) {
        String configured = PlatformConfig.get("payment.alipay.notifyUrl", null);
        return notEmpty(configured) ? configured : fallback;
    }

    private static boolean notEmpty(String s) {
        return s != null && !s.trim().isEmpty();
    }
}
