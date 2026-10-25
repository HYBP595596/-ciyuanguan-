package com.dimension.controller.commission;

import cn.hutool.core.map.MapUtil;
import com.dimension.utils.JsonResultOutputUtil;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "CreatePayment", value = "/CreatePayment")
public class CreatePaymentServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String orderNo = req.getParameter("orderNo");
        String amount = req.getParameter("amount");

        // 模拟第三方统一下单接口，返回前端需要的支付参数
        JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                .put("flag", 0)
                .put("orderNo", orderNo)
                .put("amount", amount)
                // 这个就是模拟的网关回调地址
                .put("payUrl", "/MockPayGateway?orderNo=" + orderNo + "&amount=" + amount)
                .build());
    }
}