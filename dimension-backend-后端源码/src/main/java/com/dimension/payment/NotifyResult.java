package com.dimension.payment;

import java.math.BigDecimal;

/** 异步通知验签结果 */
public class NotifyResult {
    private boolean valid;
    private String paymentNo;
    private String tradeNo;
    private BigDecimal amount;
    private String status;   // PAID
    private String raw;
    private String message;

    public static NotifyResult invalid(String message) {
        NotifyResult r = new NotifyResult();
        r.valid = false;
        r.message = message;
        return r;
    }

    public static NotifyResult valid(String paymentNo, String tradeNo, BigDecimal amount, String status, String raw) {
        NotifyResult r = new NotifyResult();
        r.valid = true;
        r.paymentNo = paymentNo;
        r.tradeNo = tradeNo;
        r.amount = amount;
        r.status = status;
        r.raw = raw;
        return r;
    }

    public boolean isValid() { return valid; }
    public String getPaymentNo() { return paymentNo; }
    public String getTradeNo() { return tradeNo; }
    public BigDecimal getAmount() { return amount; }
    public String getStatus() { return status; }
    public String getRaw() { return raw; }
    public String getMessage() { return message; }
}
