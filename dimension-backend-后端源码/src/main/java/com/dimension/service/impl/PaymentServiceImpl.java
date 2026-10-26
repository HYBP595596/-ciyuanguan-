package com.dimension.service.impl;

import com.dimension.config.PlatformConfig;
import com.dimension.entity.CommissionOrder;
import com.dimension.entity.Payment;
import com.dimension.mapper.CommissionMapper;
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

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class PaymentServiceImpl implements PaymentService {

    private final PaymentMapper paymentMapper = SessionUtil.getMapper(PaymentMapper.class);
    private final CommissionService commissionService = new CommissionServiceImpl();

    @Override
    public Payment createPayment(Long buyerId, String orderNo, String channel) {
        // 惰性关闭：先把超时未支付的订单关掉，避免给已超时的订单继续发支付单
        commissionService.closeExpiredOrders();

        CommissionOrder order = commissionService.getOrderByNo(orderNo);
        if (order == null) {
            return null;
        }
        if (buyerId != null && !buyerId.equals(order.getBuyerId())) {
            return null; // 只能为自己的订单付款
        }
        if (order.getStatus() == null || order.getStatus() != 0) {
            return null; // 只有待支付可下单支付（已关闭/已付款都拒绝）
        }
        PaymentGateway gateway = PaymentGatewayFactory.of(channel);
        if (gateway == null) {
            return null; // 不支持的渠道
        }
        if (!gateway.isConfigured()) {
            return null; // 渠道参数没配，不能开出无法支付的单子
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
        payment.setNotifyUrl(PlatformConfig.apiBase() + "/PayNotify?channel=" + gateway.channel());
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

    /**
     * 支付回调。
     *
     * 关键点：验签在事务外（纯计算），一旦验签通过，
     * 「支付单置已支付 + 订单待支付→绘制中 + 记流水 + 记日志」必须在同一个事务里，
     * 否则会出现"钱收了订单没动"或者"订单动了流水没有"的对账灾难。
     * 事务内先 SELECT ... FOR UPDATE 锁支付单，渠道重复回调只有一个线程能推进。
     */
    @Override
    public NotifyResult handleNotify(String channel, Map<String, String> params) {
        PaymentGateway gateway = PaymentGatewayFactory.of(channel);
        if (gateway == null) {
            return NotifyResult.invalid("不支持的支付渠道");
        }
        NotifyResult verified = gateway.verifyNotify(params);
        if (!verified.isValid()) {
            return verified; // 验签失败：绝不落地
        }

        NotifyResult[] outcome = new NotifyResult[1];
        SessionUtil.inTransaction(session -> {
            PaymentMapper pm = session.getMapper(PaymentMapper.class);
            CommissionMapper cm = session.getMapper(CommissionMapper.class);

            Payment payment = pm.getByPaymentNoForUpdate(verified.getPaymentNo());
            if (payment == null) {
                outcome[0] = NotifyResult.invalid("支付单不存在");
                return null;
            }
            // 幂等：已支付直接回成功，不重复记账
            if (payment.getStatus() != null && payment.getStatus() == 1) {
                outcome[0] = verified;
                return null;
            }
            if (payment.getStatus() == null || payment.getStatus() != 0) {
                outcome[0] = NotifyResult.invalid("支付单状态不可支付");
                return null;
            }
            // 金额核对：回调金额必须与支付单一致
            if (verified.getAmount() == null
                    || verified.getAmount().compareTo(payment.getAmount()) != 0) {
                outcome[0] = NotifyResult.invalid("回调金额与支付单不一致");
                return null;
            }

            int rows = pm.markPaid(payment.getPaymentNo(), verified.getTradeNo(), verified.getRaw());
            if (rows == 0) {
                // 并发回调：另一个线程已处理
                outcome[0] = verified;
                return null;
            }
            // 同一事务里驱动订单：待支付 → 绘制中（资金托管）
            CommissionTxOps.paySuccess(session, payment.getOutTradeNo(), verified.getTradeNo());
            outcome[0] = verified;
            return null;
        });

        return outcome[0] == null ? NotifyResult.invalid("回调处理失败") : outcome[0];
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
        // 先让渠道退钱（网络调用，不占数据库锁）
        PayResult result = gateway.refund(payment.getPaymentNo(), payment.getTradeNo(),
                payment.getAmount(), reason);
        if (!result.isSuccess()) {
            return false;
        }
        // 再把「支付单已退款 + 订单已退款 + REFUND 流水」放进同一事务
        return doRefund(payment.getPaymentNo(), buyerId, false);
    }

    @Override
    public boolean refundByOrder(String orderNo, String reason, Long adminId) {
        Payment payment = paymentMapper.getPaidByOutTradeNo(orderNo);
        if (payment == null) {
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
        return doRefund(payment.getPaymentNo(), adminId, true);
    }

    private boolean doRefund(String paymentNo, Long operatorId, boolean allowDelivered) {
        return Boolean.TRUE.equals(SessionUtil.inTransaction(session -> {
            PaymentMapper pm = session.getMapper(PaymentMapper.class);
            CommissionMapper cm = session.getMapper(CommissionMapper.class);

            Payment payment = pm.getByPaymentNoForUpdate(paymentNo);
            if (payment == null || payment.getStatus() == null || payment.getStatus() != 1) {
                return false;
            }
            int rows = pm.markRefunded(paymentNo);
            if (rows == 0) {
                return false;
            }
            CommissionOrder order = cm.getOrderByNoForUpdate(payment.getOutTradeNo());
            if (order == null) {
                return false;
            }
            return CommissionTxOps.refund(session, order, operatorId, allowDelivered);
        }));
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
}
