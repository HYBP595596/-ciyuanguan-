package com.dimension.service;

import com.dimension.entity.CommissionOrder;
import com.dimension.entity.CommissionPlan;
import com.dimension.entity.Transaction;
import com.dimension.entity.User;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface CommissionService {

    // ---------- 画师 ----------
    int setAcceptCommission(Long artistId, Integer accept);

    CommissionPlan addPlan(Long artistId, String title, BigDecimal price, Integer deliveryDays,
                           String coverUrl, Integer contentType);

    int updatePlanStatus(Long artistId, Long planId, Integer status);

    List<CommissionPlan> getMyPlans(Long artistId);

    List<CommissionOrder> getArtistOrders(Long artistId);

    boolean deliverOrder(Long artistId, Long orderId, String type, String url);

    // ---------- 买家 / 广场 ----------
    List<User> getAcceptingArtists(Integer pageNum, Integer pageSize);

    long getAcceptingArtistTotal();

    User getArtist(Long artistId);

    List<CommissionPlan> getArtistOnlinePlans(Long artistId);

    List<CommissionPlan> getOnlinePlans(Integer pageNum, Integer pageSize);

    long getOnlinePlanTotal();

    CommissionOrder createOrder(Long buyerId, Long planId, String requirement);

    List<CommissionOrder> getBuyerOrders(Long buyerId);

    CommissionOrder getOrder(Long orderId);

    CommissionOrder getOrderByNo(String orderNo);

    /** 支付成功回调：把订单置为「绘制中」并记流水（事务内由支付回调驱动） */
    boolean paySuccess(String orderNo, String transactionId);

    /** 买家验收：订单完成 + 平台抽成 + 画师余额入账 */
    boolean confirmOrder(Long buyerId, Long orderId);

    /**
     * 买家验收，返回结算明细（订单金额/平台抽成/画师实收）。
     * 订单不在「待验收」时返回 null。
     */
    Map<String, Object> confirmOrderWithSettlement(Long buyerId, Long orderId);

    /** 退款成功后：订单置为已退款(4) 并记 REFUND 流水 */
    boolean refundOrder(String orderNo, Long operatorId);

    /** 管理端强制退款（允许「待验收」状态） */
    boolean refundOrderByAdmin(String orderNo, Long adminId);

    /** 买家手动取消（仅待支付），同时关闭待支付单 */
    boolean cancelOrder(Long buyerId, Long orderId);

    /** 管理端强制关闭（仅待支付） */
    boolean closeOrderByAdmin(Long orderId, Long adminId);

    /** 超时未支付自动关闭，返回本次关闭数量（定时任务 + 惰性调用） */
    int closeExpiredOrders();

    // ---------- 管理端 ----------
    List<CommissionOrder> getAdminOrders(String keyword, Integer status,
                                         Integer pageNum, Integer pageSize);

    long getAdminOrderTotal(String keyword, Integer status);

    Map<String, Object> getOrderStats();

    List<Transaction> getTransactions(String orderNo);
}
