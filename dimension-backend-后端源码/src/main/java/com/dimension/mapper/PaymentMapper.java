package com.dimension.mapper;

import com.dimension.entity.Payment;
import org.apache.ibatis.annotations.Param;

public interface PaymentMapper {

    int insertPayment(Payment payment);

    Payment getByPaymentNo(@Param("paymentNo") String paymentNo);

    /** 事务内加行锁读支付单（回调并发时保证只结算一次） */
    Payment getByPaymentNoForUpdate(@Param("paymentNo") String paymentNo);

    /** 同一笔业务订单复用未支付的支付单，避免重复下单 */
    Payment getPendingByOutTradeNo(@Param("outTradeNo") String outTradeNo);

    /** 取该业务订单最新的一笔支付单（退款时用） */
    Payment getLatestByOutTradeNo(@Param("outTradeNo") String outTradeNo);

    /** 业务订单当前已支付成功的那笔支付单（管理员强制退款用） */
    Payment getPaidByOutTradeNo(@Param("outTradeNo") String outTradeNo);

    int markPaid(@Param("paymentNo") String paymentNo,
                 @Param("tradeNo") String tradeNo,
                 @Param("rawNotify") String rawNotify);

    int markClosed(@Param("paymentNo") String paymentNo);

    /** 订单关闭/取消时，把该订单所有待支付单一起关掉 */
    int closePendingByOutTradeNo(@Param("outTradeNo") String outTradeNo);

    int markRefunded(@Param("paymentNo") String paymentNo);
}
