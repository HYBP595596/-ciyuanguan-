package com.dimension.controller.payment;

import com.dimension.payment.NotifyResult;
import com.dimension.service.PaymentService;
import com.dimension.service.impl.PaymentServiceImpl;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 第三方支付异步通知统一入口 /PayNotify?channel=MOCK
 * 约定：验签通过 → 返回纯文本 SUCCESS；否则返回 FAIL（渠道会重试）
 */
@WebServlet(name = "PayNotify", value = "/PayNotify")
public class PayNotifyServlet extends HttpServlet {

    private final PaymentService paymentService = new PaymentServiceImpl();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handle(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // 真实渠道一般只允许 POST；这里为了模拟方便 GET 也接受
        handle(req, resp);
    }

    private void handle(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String channel = req.getParameter("channel");
        Map<String, String> params = new HashMap<>();
        for (Map.Entry<String, String[]> entry : req.getParameterMap().entrySet()) {
            String[] values = entry.getValue();
            if (values != null && values.length > 0) {
                params.put(entry.getKey(), values[0]);
            }
        }

        NotifyResult result = paymentService.handleNotify(channel, params);
        if (result.isValid()) {
            resp.getWriter().write("SUCCESS");
        } else {
            resp.setStatus(400);
            resp.getWriter().write("FAIL:" + result.getMessage());
        }
    }
}
