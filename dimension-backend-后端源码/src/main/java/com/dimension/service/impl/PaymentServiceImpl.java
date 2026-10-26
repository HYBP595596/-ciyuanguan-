package com.dimension.service.impl;

import com.dimension.entity.CommissionOrder;
import com.dimension.entity.Payment;
import com.dimension.mapper.PaymentMapper;
import com.dimension.payment.MockPaymentGateway;
import com.dimension.payment.NotifyResult;
import com.dimension.payment.PayRequest;
import com.dimension.payment.PayResult;
import com.dimension.payment.PaymentGateway;
import com.dimension.payment.PaymentGatewayFactory;
import com.dimension.service.CommissionService;
import com.dimension.service.PaymentService;
import com.dimension.utils.SessionUtil;

import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;

public class PaymentServiceImpl implements PaymentService {

    private final PaymentMapper paymentMapper = SessionUtil.getMapper(PaymentMapper.class);
    private final CommissionService commissionService = new CommissionServiceImpl();
    private static final Properties CONFIG = loadConfig();

    @Override
    public Payment createPayment(Long buyerId, String orderNo, String channel) {
        CommissionOrder order = commissionService.getOrderByNo(orderNo);
        if (order == null) {
            return null;
        }
        if (buyerId != null && !buyerId.equals(order.getBuyerId())) {
            return null; // 只能为自己的订单付款
        }
        if (order.getStatus() == null || order.getStatus() != 0) {
            return null; // 只有待支付可下单支付
        }
        PaymentGateway gateway = PaymentGatewayFactory.of(channel);
        if (gateway == null) {
            return null; // 不支持的渠道
        }

        // 复用未支付的支付单，避免刷单
        Payment pending = paymentMapper.getPendingByOutTradeNo(orderNo);
        if (pending != null && pending.getAmount() != null
                && pending.getAmount().compareTo(order.getAmount()) == 0) {
            return pending;
        }

        Payment payment = new Payment();
        payment.setPaymentNo(generatePaymentNo());
        payment.setOutTradeNo(orderNo);
        payment.setBuyerId(order.getBuyerId());
        payment.setChannel(gateway.channel());
        payment.setAmount(order.getAmount());
        payment.setSubject(buildSubject(order));
        payment.setStatus(0);
        payment.setNotifyUrl(apiBase() + "/PayNotify?channel=" + gateway.channel());
        paymentMapper.insertPayment(payment);
        return payment;
    }

    @Override
    public Map<String, Object> payParams(Payment payment) {
        PaymentGateway gateway = PaymentGatewayFactory.of(payment.getChannel());
        if (gateway == null) {
            return null;
        }
        PayRequest request = new PayRequest();
        request.setPaymentNo(payment.getPaymentNo());
        request.setOutTradeNo(payment.getOutTradeNo());
        request.setAmount(payment.getAmount());
        request.setSubject(payment.getSubject());
        request.setNotifyUrl(payment.getNotifyUrl());
        return gateway.createPay(request);
    }

    @Override
    public NotifyResult handleNotify(String channel, Map<String, String> params) {
        PaymentGateway gateway = PaymentGatewayFactory.of(channel);
        if (gateway == null) {
            return NotifyResult.invalid("不支持的支付渠道");
        }
        NotifyResult result = gateway.verifyNotify(params);
        if (!result.isValid()) {
            return result; // 验签失败：绝不落地
        }

        Payment payment = paymentMapper.getByPaymentNo(result.getPaymentNo());
        if (payment == null) {
            return NotifyResult.invalid("支付单不存在");
        }
        // 幂等：已支付直接回成功，不重复记账
        if (payment.getStatus() != null && payment.getStatus() == 1) {
            return result;
        }
        if (payment.getStatus() == null || payment.getStatus() != 0) {
            return NotifyResult.invalid("支付单状态不可支付");
        }
        // 金额核对：回调金额必须与支付单一致
        if (result.getAmount() == null || result.getAmount().compareTo(payment.getAmount()) != 0) {
            return NotifyResult.invalid("回调金额与支付单不一致");
        }

        int rows = paymentMapper.markPaid(payment.getPaymentNo(), result.getTradeNo(), result.getRaw());
        if (rows == 0) {
            // 并发回调：另一个线程已处理，按幂等返回成功
            return result;
        }
        // 驱动业务：订单 待支付 → 绘制中（资金托管）
        commissionService.paySuccess(payment.getOutTradeNo(), result.getTradeNo());
        return result;
    }

    @Override
    public Payment query(String paymentNo) {
        return paymentMapper.getByPaymentNo(paymentNo);
    }

    @Override
    public Payment queryByOutTradeNo(String outTradeNo) {
        return paymentMapper.getLatestByOutTradeNo(outTradeNo);
    }

    @Override
    public boolean refund(Long buyerId, String paymentNo, String reason) {
        Payment payment = paymentMapper.getByPaymentNo(paymentNo);
        if (payment == null || payment.getStatus() == null || payment.getStatus() != 1) {
            return false;
        }
        if (buyerId != null && !buyerId.equals(payment.getBuyerId())) {
            return false;
        }
        PaymentGateway gateway = PaymentGatewayFactory.of(payment.getChannel());
        if (gateway == null) {
            return false;
        }
        PayResult result = gateway.refund(payment.getPaymentNo(), payment.getTradeNo(),
                payment.getAmount(), reason);
        if (!result.isSuccess()) {
            return false;
        }
        int rows = paymentMapper.markRefunded(paymentNo);
        if (rows == 0) {
            return false;
        }
        return commissionService.refundOrder(payment.getOutTradeNo(), buyerId);
    }

    @Override
    public Map<String, String> buildMockCashier(Payment payment) {
        PaymentGateway gateway = PaymentGatewayFactory.of(payment.getChannel());
        if (gateway instanceof MockPaymentGateway) {
            return ((MockPaymentGateway) gateway)
                    .buildSignedNotify(payment.getPaymentNo(), payment.getAmount().toPlainString());
        }
        return null;
    }

    private String buildSubject(CommissionOrder order) {
        String title = order.getPlanTitle() == null ? "约稿" : order.getPlanTitle();
        return "次元馆约稿-" + title;
    }

    private String generatePaymentNo() {
        long ts = System.currentTimeMillis();
        int rnd = ThreadLocalRandom.current().nextInt(100000, 999999);
        return "P" + ts + rnd;
    }

    private static String apiBase() {
        return CONFIG.getProperty("payment.api.base", "/dimension_dev_01_war_exploded");
    }

    private static Properties loadConfig() {
        Properties p = new Properties();
        try (InputStream in = PaymentServiceImpl.class.getClassLoader()
                .getResourceAsStream("payment.properties")) {
            if (in != null) {
                p.load(in);
            }
        } catch (Exception ignored) {
        }
        return p;
    }
}
