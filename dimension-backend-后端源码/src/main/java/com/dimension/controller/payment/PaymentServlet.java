package com.dimension.controller.payment;

import cn.hutool.core.map.MapUtil;
import com.dimension.entity.Payment;
import com.dimension.service.PaymentService;
import com.dimension.service.impl.PaymentServiceImpl;
import com.dimension.utils.JsonResultOutputUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * 平台支付统一入口 /Payment?action=xxx
 *   create      预下单，返回支付参数（真实渠道返回二维码/跳转地址）
 *   query       查单
 *   mockCashier 模拟渠道收银台：生成带签名的回调参数（真实场景由支付公司完成）
 *   refund      退款
 */
@WebServlet(name = "Payment", value = "/Payment")
public class PaymentServlet extends HttpServlet {

    private final PaymentService paymentService = new PaymentServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handle(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handle(req, resp);
    }

    private void handle(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String action = req.getParameter("action");
        if (action == null) action = "";
        try {
            switch (action) {
                case "create": {
                    String orderNo = req.getParameter("orderNo");
                    Long buyerId = req.getParameter("buyerId") == null
                            ? null : Long.valueOf(req.getParameter("buyerId"));
                    String channel = req.getParameter("channel");
                    Payment payment = paymentService.createPayment(buyerId, orderNo, channel);
                    if (payment == null) {
                        JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                                .put("flag", 1).put("message", "订单不可支付（不存在/非本人/状态不符/渠道不支持）").build());
                        return;
                    }
                    Map<String, Object> params = paymentService.payParams(payment);
                    JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                            .put("flag", 0)
                            .put("paymentNo", payment.getPaymentNo())
                            .put("outTradeNo", payment.getOutTradeNo())
                            .put("amount", payment.getAmount())
                            .put("channel", payment.getChannel())
                            .put("payParams", params)
                            .build());
                    return;
                }
                case "query": {
                    String paymentNo = req.getParameter("paymentNo");
                    Payment payment = paymentService.query(paymentNo);
                    if (payment == null) {
                        JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                                .put("flag", 1).put("message", "支付单不存在").build());
                        return;
                    }
                    JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                            .put("flag", 0).put("payment", payment).build());
                    return;
                }
                case "mockCashier": {
                    String paymentNo = req.getParameter("paymentNo");
                    Payment payment = paymentService.query(paymentNo);
                    if (payment == null || payment.getStatus() == null || payment.getStatus() != 0) {
                        JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                                .put("flag", 1).put("message", "支付单不存在或不可支付").build());
                        return;
                    }
                    Map<String, String> signed = paymentService.buildMockCashier(payment);
                    if (signed == null) {
                        JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                                .put("flag", 1).put("message", "该渠道不是模拟渠道").build());
                        return;
                    }
                    JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                            .put("flag", 0)
                            .put("paymentNo", payment.getPaymentNo())
                            .put("amount", payment.getAmount())
                            .put("notifyUrl", payment.getNotifyUrl())
                            .put("params", signed)
                            .build());
                    return;
                }
                case "byOrder": {
                    String outTradeNo = req.getParameter("outTradeNo");
                    Payment payment = paymentService.queryByOutTradeNo(outTradeNo);
                    if (payment == null) {
                        JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                                .put("flag", 1).put("message", "该订单还没有支付单").build());
                        return;
                    }
                    JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                            .put("flag", 0).put("payment", payment).build());
                    return;
                }
                case "refund": {
                    String paymentNo = req.getParameter("paymentNo");
                    String reason = req.getParameter("reason");
                    Long buyerId = req.getParameter("buyerId") == null
                            ? null : Long.valueOf(req.getParameter("buyerId"));
                    boolean ok = paymentService.refund(buyerId, paymentNo, reason);
                    JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                            .put("flag", ok ? 0 : 1)
                            .put("message", ok ? "退款成功" : "退款失败：支付单不可退或已处理")
                            .build());
                    return;
                }
                default:
                    JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                            .put("flag", 1).put("message", "未知操作: " + action).build());
            }
        } catch (Exception e) {
            JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                    .put("flag", 1).put("message", "参数错误: " + e.getMessage()).build());
        }
    }
}
