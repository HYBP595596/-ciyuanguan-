package com.dimension.controller.commission;

import com.dimension.entity.Transaction;
import com.dimension.mapper.CommissionMapper;
import com.dimension.utils.SessionUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;

@WebServlet(name = "MockPayGateway", value = "/MockPayGateway")
public class MockPayGatewayServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String orderNo = req.getParameter("orderNo");
        String amount = req.getParameter("amount");

        CommissionMapper mapper = SessionUtil.getMapper(CommissionMapper.class);

        // 【真实场景：这里必须验签，防止伪造支付成功】

        // 1. 幂等性检查（真实项目必须有，这里简化：假设没重复）
        // 2. 更新订单状态为 1 (已支付/托管中)
        mapper.updateOrderStatusByOrderNo(orderNo, 1);

        // 3. 记录第三方交易号
        String mockTransactionId = "MOCK_" + System.currentTimeMillis();
        mapper.updateTransactionId(orderNo, mockTransactionId);

        // 4. 写入资金流水表（这里为了测试，直接写死了buyerId=11, artistId=1，真正开发要查订单表）
        Transaction trx = new Transaction();
        trx.setOrderNo(orderNo);
        trx.setBuyerId(11L); // 模拟买家
        trx.setArtistId(1L); // 模拟画师
        trx.setAmount(new BigDecimal(amount));
        trx.setType("PAY");
        trx.setTransactionId(mockTransactionId);
        mapper.insertTransaction(trx);

        // 5. 记录日志
        mapper.insertOrderLogByOrderNo(orderNo, 11L, "PAY_SUCCESS");

        // 必须返回 SUCCESS
        resp.getWriter().write("SUCCESS");
    }
}