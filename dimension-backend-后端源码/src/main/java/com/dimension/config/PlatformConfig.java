package com.dimension.config;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Properties;

/**
 * 平台配置（读 resources/payment.properties）。
 * 抽成比例、订单超时时间这类"运营参数"都放这里，改配置即可，不用改代码。
 *
 *   platform.fee.rate                  平台抽成比例，0.10 = 10%
 *   commission.order.timeout.minutes   待支付订单超时关闭时长（分钟）
 *   payment.api.base                   对外回调地址前缀（部署到别的 context 时改这里）
 */
public final class PlatformConfig {

    private static final String FILE = "payment.properties";

    private static final Properties PROPS = load();

    private PlatformConfig() {
    }

    public static String get(String key, String defaultValue) {
        String v = PROPS.getProperty(key);
        return (v == null || v.trim().isEmpty()) ? defaultValue : v.trim();
    }

    public static boolean getBool(String key, boolean defaultValue) {
        String v = get(key, null);
        if (v == null) {
            return defaultValue;
        }
        return "true".equalsIgnoreCase(v) || "1".equals(v) || "yes".equalsIgnoreCase(v);
    }

    /**
     * 平台抽成比例。
     * 兜底 0.10；异常值（负数/超过 50%）夹到合法区间，避免配置手滑把账算穿。
     */
    public static BigDecimal feeRate() {
        String raw = get("platform.fee.rate", "0.10");
        BigDecimal rate;
        try {
            rate = new BigDecimal(raw);
        } catch (NumberFormatException e) {
            rate = new BigDecimal("0.10");
        }
        if (rate.compareTo(BigDecimal.ZERO) < 0) {
            rate = BigDecimal.ZERO;
        }
        if (rate.compareTo(new BigDecimal("0.5")) > 0) {
            rate = new BigDecimal("0.5");
        }
        return rate;
    }

    /** 待支付订单超时关闭时长（分钟） */
    public static int orderTimeoutMinutes() {
        String raw = get("commission.order.timeout.minutes", "30");
        try {
            int minutes = Integer.parseInt(raw);
            return minutes < 1 ? 30 : minutes;
        } catch (NumberFormatException e) {
            return 30;
        }
    }

    public static String apiBase() {
        return get("payment.api.base", "/dimension_dev_01_war_exploded");
    }

    /** 按比例算抽成（四舍五入到分） */
    public static BigDecimal calcFee(BigDecimal amount) {
        if (amount == null) {
            return BigDecimal.ZERO;
        }
        return amount.multiply(feeRate()).setScale(2, RoundingMode.HALF_UP);
    }

    private static Properties load() {
        Properties p = new Properties();
        try (InputStream in = PlatformConfig.class.getClassLoader().getResourceAsStream(FILE)) {
            if (in != null) {
                p.load(in);
            }
        } catch (Exception ignored) {
            // 配置缺失时全部走默认值，保证项目一定能跑起来
        }
        return p;
    }
}
