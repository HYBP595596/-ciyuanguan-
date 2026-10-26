package com.dimension.service;

import com.dimension.entity.CommissionOrder;
import com.dimension.entity.CommissionPlan;
import com.dimension.entity.User;

import java.math.BigDecimal;
import java.util.List;

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

    /** 支付成功回调：把订单置为「绘制中」并记流水 */
    boolean paySuccess(String orderNo, String transactionId);

    /** 买家验收：订单完成，给画师记一笔收入流水 */
    boolean confirmOrder(Long buyerId, Long orderId);

    /** 退款成功后：订单置为已退款(4) 并记 REFUND 流水 */
    boolean refundOrder(String orderNo, Long operatorId);
}
