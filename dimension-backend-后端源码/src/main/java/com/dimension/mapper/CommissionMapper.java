package com.dimension.mapper;

import com.dimension.entity.CommissionOrder;
import com.dimension.entity.CommissionPlan;
import com.dimension.entity.Transaction;
import com.dimension.entity.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CommissionMapper {

    // ==================== 方案(档期) ====================
    CommissionPlan getPlanById(@Param("planId") Long planId);

    /** onlyOnline=1 时只返回上架中的方案 */
    List<CommissionPlan> getPlansByArtist(@Param("artistId") Long artistId,
                                          @Param("onlyOnline") Integer onlyOnline);

    int insertPlan(CommissionPlan plan);

    int updatePlanStatus(@Param("planId") Long planId,
                         @Param("artistId") Long artistId,
                         @Param("status") Integer status);

    List<CommissionPlan> getOnlinePlans(@Param("pageNum") Integer pageNum,
                                        @Param("pageSize") Integer pageSize);

    long getOnlinePlanTotal();

    // ==================== 画师广场 ====================
    /** 已通过认证且开启了接稿开关的画师 */
    List<User> getAcceptingArtists(@Param("pageNum") Integer pageNum,
                                   @Param("pageSize") Integer pageSize);

    long getAcceptingArtistTotal();

    User getArtistById(@Param("artistId") Long artistId);

    int updateAcceptCommission(@Param("userId") Long userId, @Param("accept") Integer accept);

    // ==================== 订单 ====================
    int insertOrder(CommissionOrder order);

    CommissionOrder getOrderByNo(@Param("orderNo") String orderNo);

    CommissionOrder getOrderById(@Param("orderId") Long orderId);

    List<CommissionOrder> getOrdersByBuyer(@Param("buyerId") Long buyerId);

    List<CommissionOrder> getOrdersByArtist(@Param("artistId") Long artistId);

    int updateOrderStatus(@Param("orderId") Long orderId, @Param("status") Integer status);

    int setDeliveryPreview(@Param("orderId") Long orderId, @Param("url") String url);

    int setDeliveryFinal(@Param("orderId") Long orderId, @Param("url") String url);

    // ==================== 流水 / 日志 ====================
    void insertTransaction(Transaction transaction);

    List<Transaction> getTransactionsByOrderNo(@Param("orderNo") String orderNo);

    void updateOrderStatusByOrderNo(@Param("orderNo") String orderNo, @Param("status") Integer status);

    void updateTransactionId(@Param("orderNo") String orderNo, @Param("transactionId") String transactionId);

    void insertOrderLogByOrderNo(@Param("orderNo") String orderNo,
                                 @Param("operatorId") Long operatorId,
                                 @Param("action") String action);

    void insertOrderLog(@Param("orderId") Long orderId,
                        @Param("operatorId") Long operatorId,
                        @Param("action") String action);
}
