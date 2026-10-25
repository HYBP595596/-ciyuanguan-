package com.dimension.mapper;

import com.dimension.entity.CommissionOrder;
import com.dimension.entity.Transaction;
import org.apache.ibatis.annotations.Param;

public interface CommissionMapper {
    // 更新订单状态
    void updateOrderStatusByOrderNo(@Param("orderNo") String orderNo, @Param("status") Integer status);

    // 更新第三方交易流水号
    void updateTransactionId(@Param("orderNo") String orderNo, @Param("transactionId") String transactionId);

    // 插入资金流水
    void insertTransaction(Transaction transaction);

    // 插入订单操作日志
    void insertOrderLogByOrderNo(@Param("orderNo") String orderNo, @Param("operatorId") Long operatorId, @Param("action") String action);
}