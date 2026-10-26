package com.dimension.service.impl;

import com.dimension.config.PlatformConfig;
import com.dimension.entity.CommissionOrder;
import com.dimension.entity.Transaction;
import com.dimension.mapper.ArtistBalanceMapper;
import com.dimension.mapper.CommissionMapper;
import com.dimension.mapper.PaymentMapper;
import org.apache.ibatis.session.SqlSession;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单状态流转的「事务内实现」。
 *
 * 这里是唯一改订单状态的地方：扣款、结算、退款、关闭都必须是
 *   锁订单行 → 校验前置状态 → 改状态 → 写流水/日志
 * 一整个事务，任何一步失败全部回滚。
 *
 * 所有方法都必须在 {@link com.dimension.utils.SessionUtil#inTransaction} 的回调里调用，
 * mapper 必须从传入的 session 上取（否则会另开连接，脱离事务）。
 */
final class CommissionTxOps {

    private CommissionTxOps() {
    }

    // ==================== 支付成功：待支付 → 绘制中 ====================

    /**
     * 资金到账后推进订单。
     *
     * @return true = 已处理（或本来就是已支付，幂等）
     */
    static boolean paySuccess(SqlSession session, String orderNo, String tradeNo) {
        CommissionMapper cm = session.getMapper(CommissionMapper.class);
        CommissionOrder order = cm.getOrderByNoForUpdate(orderNo);
        if (order == null || order.getStatus() == null) {
            return false;
        }
        if (order.getStatus() != 0) {
            // 已支付过 → 幂等；已关闭却收到钱 → 记日志，交由管理端退款
            if (order.getStatus() == 5) {
                cm.insertOrderLogByOrderNo(orderNo, order.getBuyerId(), "PAY_AFTER_CLOSE");
            }
            return true;
        }
        // 前置状态守卫：并发回调只有一个能把 0 改成 1
        int rows = cm.startOrderByOrderNo(orderNo);
        if (rows == 0) {
            return true;
        }
        cm.updateTransactionId(orderNo, tradeNo);

        Transaction pay = new Transaction();
        pay.setOrderNo(orderNo);
        pay.setBuyerId(order.getBuyerId());
        pay.setArtistId(order.getArtistId());
        pay.setAmount(order.getAmount());
        pay.setType("PAY");
        pay.setTransactionId(tradeNo);
        cm.insertTransaction(pay);

        cm.insertOrderLogByOrderNo(orderNo, order.getBuyerId(), "PAY_SUCCESS");
        return true;
    }

    // ==================== 验收：待验收 → 已完成 + 抽成 + 画师余额 ====================

    /**
     * 买家验收。款项拆两笔流水：画师 INCOME（实收）+ 平台 PLATFORM_FEE（抽成），
     * 同时把实收累加到画师余额，之后才能提现。
     *
     * @return null = 状态不允许；否则返回 [订单金额, 平台抽成, 画师实收]
     */
    static BigDecimal[] confirmComplete(SqlSession session, CommissionOrder order, Long operatorId) {
        CommissionMapper cm = session.getMapper(CommissionMapper.class);

        BigDecimal amount = order.getAmount() == null ? BigDecimal.ZERO : order.getAmount();
        BigDecimal fee = PlatformConfig.calcFee(amount);
        BigDecimal income = amount.subtract(fee);

        int rows = cm.completeOrder(order.getId(), fee, income);
        if (rows == 0) {
            return null; // 不是「待验收」状态，或已被并发处理
        }

        Transaction incomeTx = tx(order, "INCOME", income, order.getTransactionId());
        cm.insertTransaction(incomeTx);

        if (fee.compareTo(BigDecimal.ZERO) > 0) {
            Transaction feeTx = tx(order, "PLATFORM_FEE", fee, order.getTransactionId());
            cm.insertTransaction(feeTx);
        }

        ArtistBalanceMapper bm = session.getMapper(ArtistBalanceMapper.class);
        if (income.compareTo(BigDecimal.ZERO) > 0) {
            bm.addIncome(order.getArtistId(), income);
        }

        cm.insertOrderLog(order.getId(),
                operatorId == null ? order.getBuyerId() : operatorId,
                "CONFIRM_COMPLETE");
        return new BigDecimal[]{amount, fee, income};
    }

    // ==================== 退款：已付款 → 已退款 ====================

    /**
     * @param allowDelivered 管理端强制退款允许「待验收」；买家自助退款只允许「绘制中」
     * @return true = 本次真的完成了退款状态流转
     */
    static boolean refund(SqlSession session, CommissionOrder order, Long operatorId, boolean allowDelivered) {
        CommissionMapper cm = session.getMapper(CommissionMapper.class);

        int rows = cm.markRefundedOrder(order.getId(), allowDelivered);
        if (rows == 0) {
            return false;
        }
        cm.insertOrderLog(order.getId(),
                operatorId == null ? order.getBuyerId() : operatorId,
                allowDelivered ? "ADMIN_REFUND" : "REFUND_SUCCESS");

        Transaction refundTx = tx(order, "REFUND", order.getAmount(), order.getTransactionId());
        cm.insertTransaction(refundTx);
        return true;
    }

    // ==================== 取消 / 超时关闭：待支付 → 已关闭 ====================

    /**
     * 关闭一个待支付订单，并把它名下的待支付单一起关掉（不然渠道还能收钱）。
     *
     * @return true = 本次真的关掉了
     */
    static boolean closeOrder(SqlSession session, CommissionOrder order, Long operatorId, String action) {
        CommissionMapper cm = session.getMapper(CommissionMapper.class);
        int rows = cm.closeOrder(order.getId());
        if (rows == 0) {
            return false;
        }
        session.getMapper(PaymentMapper.class).closePendingByOutTradeNo(order.getOrderNo());
        cm.insertOrderLog(order.getId(),
                operatorId == null ? order.getBuyerId() : operatorId,
                action);
        return true;
    }

    /** 超时关闭：一次最多处理 limit 单，返回实际关闭数量 */
    static int closeExpired(SqlSession session, int minutes, int limit) {
        CommissionMapper cm = session.getMapper(CommissionMapper.class);
        List<CommissionOrder> expired = cm.getExpiredOrders(minutes, limit);
        if (expired == null || expired.isEmpty()) {
            return 0;
        }
        int closed = 0;
        for (CommissionOrder order : expired) {
            if (closeOrder(session, order, null, "CLOSE_TIMEOUT")) {
                closed++;
            }
        }
        return closed;
    }

    private static Transaction tx(CommissionOrder order, String type, BigDecimal amount, String transactionId) {
        Transaction t = new Transaction();
        t.setOrderNo(order.getOrderNo());
        t.setBuyerId(order.getBuyerId());
        t.setArtistId(order.getArtistId());
        t.setAmount(amount);
        t.setType(type);
        t.setTransactionId(transactionId);
        return t;
    }
}
