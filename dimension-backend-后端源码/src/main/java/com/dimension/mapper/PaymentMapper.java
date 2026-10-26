package com.dimension.mapper;

import com.dimension.entity.Payment;
import org.apache.ibatis.annotations.Param;

public interface PaymentMapper {

    int insertPayment(Payment payment);

    Payment getByPaymentNo(@Param("paymentNo") String paymentNo);

    /** 同一笔业务订单复用未支付的支付单，避免重复下单 */
    Payment getPendingByOutTradeNo(@Param("outTradeNo") String outTradeNo);

    /** 取该业务订单最新的一笔支付单（退款时用） */
    Payment getLatestByOutTradeNo(@Param("outTradeNo") String outTradeNo);

    int markPaid(@Param("paymentNo") String paymentNo,
                 @Param("tradeNo") String tradeNo,
                 @Param("rawNotify") String rawNotify);

    int markClosed(@Param("paymentNo") String paymentNo);

    int markRefunded(@Param("paymentNo") String paymentNo);
}
