package com.dimension.mapper;

import com.dimension.entity.CommissionOrder;
import com.dimension.entity.CommissionPlan;
import com.dimension.entity.Transaction;
import com.dimension.entity.User;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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

    /** 事务内加行锁读订单（状态流转前必须先锁） */
    CommissionOrder getOrderByIdForUpdate(@Param("orderId") Long orderId);

    CommissionOrder getOrderByNoForUpdate(@Param("orderNo") String orderNo);

    List<CommissionOrder> getOrdersByBuyer(@Param("buyerId") Long buyerId);

    List<CommissionOrder> getOrdersByArtist(@Param("artistId") Long artistId);

    int updateOrderStatus(@Param("orderId") Long orderId, @Param("status") Integer status);

    int setDeliveryPreview(@Param("orderId") Long orderId, @Param("url") String url);

    int setDeliveryFinal(@Param("orderId") Long orderId, @Param("url") String url);

    // ==================== 状态机（带前置状态，防止并发重复流转） ====================

    /** 待支付 → 绘制中（资金托管），只有 status=0 能成功 */
    int startOrderByOrderNo(@Param("orderNo") String orderNo);

    /** 待验收 → 已完成，同时写入抽成与画师实收 */
    int completeOrder(@Param("orderId") Long orderId,
                      @Param("platformFee") BigDecimal platformFee,
                      @Param("artistIncome") BigDecimal artistIncome);

    /** 已付款(绘制中/待验收) → 已退款 */
    int markRefundedOrder(@Param("orderId") Long orderId,
                          @Param("allowDelivered") boolean allowDelivered);

    /** 待支付 → 已关闭（超时或取消） */
    int closeOrder(@Param("orderId") Long orderId);

    /** 超时未支付的订单（懒关闭/定时任务用） */
    List<CommissionOrder> getExpiredOrders(@Param("minutes") int minutes,
                                           @Param("limit") int limit);

    // ==================== 管理端 ====================
    List<CommissionOrder> getAdminOrders(@Param("keyword") String keyword,
                                         @Param("status") Integer status,
                                         @Param("pageNum") Integer pageNum,
                                         @Param("pageSize") Integer pageSize);

    long getAdminOrderTotal(@Param("keyword") String keyword,
                            @Param("status") Integer status);

    Map<String, Object> getOrderStats();

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
