package com.dimension.payment;

/** 查单 / 退款结果 */
public class PayResult {
    private boolean success;
    private String tradeNo;
    private String status;   // SUCCESS / NOTPAY / CLOSED / REFUND
    private String message;

    public static PayResult ok(String tradeNo, String status) {
        PayResult r = new PayResult();
        r.success = true;
        r.tradeNo = tradeNo;
        r.status = status;
        return r;
    }

    public static PayResult fail(String message) {
        PayResult r = new PayResult();
        r.success = false;
        r.message = message;
        return r;
    }

    public boolean isSuccess() { return success; }
    public String getTradeNo() { return tradeNo; }
    public String getStatus() { return status; }
    public String getMessage() { return message; }
}
