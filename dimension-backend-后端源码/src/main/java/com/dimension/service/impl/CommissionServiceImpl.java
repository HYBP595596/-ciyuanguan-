package com.dimension.service.impl;

import com.dimension.entity.CommissionOrder;
import com.dimension.entity.CommissionPlan;
import com.dimension.entity.Transaction;
import com.dimension.entity.User;
import com.dimension.mapper.CommissionMapper;
import com.dimension.service.CommissionService;
import com.dimension.utils.SessionUtil;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class CommissionServiceImpl implements CommissionService {

    private final CommissionMapper mapper = SessionUtil.getMapper(CommissionMapper.class);

    // ==================== 画师 ====================

    @Override
    public int setAcceptCommission(Long artistId, Integer accept) {
        User artist = mapper.getArtistById(artistId);
        if (artist == null || artist.getArtistStatus() == null || artist.getArtistStatus() != 2) {
            return -1; // 未通过画师认证
        }
        return mapper.updateAcceptCommission(artistId, accept);
    }

    @Override
    public CommissionPlan addPlan(Long artistId, String title, BigDecimal price,
                                  Integer deliveryDays, String coverUrl, Integer contentType) {
        User artist = mapper.getArtistById(artistId);
        if (artist == null || artist.getArtistStatus() == null || artist.getArtistStatus() != 2) {
            return null;
        }
        CommissionPlan plan = new CommissionPlan();
        plan.setArtistId(artistId);
        plan.setTitle(title);
        plan.setPrice(price);
        plan.setDeliveryDays(deliveryDays);
        plan.setCoverUrl(coverUrl);
        plan.setContentType(contentType == null ? 1 : contentType);
        plan.setStatus(1); // 直接上架（Pixiv 风格：画师自行管理上下架）
        mapper.insertPlan(plan);
        return plan;
    }

    @Override
    public int updatePlanStatus(Long artistId, Long planId, Integer status) {
        return mapper.updatePlanStatus(planId, artistId, status);
    }

    @Override
    public List<CommissionPlan> getMyPlans(Long artistId) {
        return mapper.getPlansByArtist(artistId, 0);
    }

    @Override
    public List<CommissionOrder> getArtistOrders(Long artistId) {
        return mapper.getOrdersByArtist(artistId);
    }

    @Override
    public boolean deliverOrder(Long artistId, Long orderId, String type, String url) {
        CommissionOrder order = mapper.getOrderById(orderId);
        if (order == null || !artistId.equals(order.getArtistId())) {
            return false;
        }
        // 只有「绘制中」的订单可以交稿
        if (order.getStatus() == null || order.getStatus() != 1) {
            return false;
        }
        if ("preview".equals(type)) {
            mapper.setDeliveryPreview(orderId, url);
            mapper.insertOrderLog(orderId, artistId, "DELIVER_PREVIEW");
        } else {
            mapper.setDeliveryFinal(orderId, url);
            mapper.insertOrderLog(orderId, artistId, "DELIVER_FINAL");
        }
        return true;
    }

    // ==================== 买家 / 广场 ====================

    @Override
    public List<User> getAcceptingArtists(Integer pageNum, Integer pageSize) {
        return mapper.getAcceptingArtists(pageNum, pageSize);
    }

    @Override
    public long getAcceptingArtistTotal() {
        return mapper.getAcceptingArtistTotal();
    }

    @Override
    public User getArtist(Long artistId) {
        return mapper.getArtistById(artistId);
    }

    @Override
    public List<CommissionPlan> getArtistOnlinePlans(Long artistId) {
        return mapper.getPlansByArtist(artistId, 1);
    }

    @Override
    public List<CommissionPlan> getOnlinePlans(Integer pageNum, Integer pageSize) {
        return mapper.getOnlinePlans(pageNum, pageSize);
    }

    @Override
    public long getOnlinePlanTotal() {
        return mapper.getOnlinePlanTotal();
    }

    @Override
    public CommissionOrder createOrder(Long buyerId, Long planId, String requirement) {
        CommissionPlan plan = mapper.getPlanById(planId);
        if (plan == null || plan.getStatus() == null || plan.getStatus() != 1) {
            return null;
        }
        if (plan.getArtistId().equals(buyerId)) {
            return null; // 不能给自己的方案下单
        }
        CommissionOrder order = new CommissionOrder();
        order.setOrderNo(generateOrderNo());
        order.setPlanId(planId);
        order.setBuyerId(buyerId);
        order.setArtistId(plan.getArtistId());
        order.setRequirement(requirement);
        order.setAmount(plan.getPrice());
        order.setContentType(plan.getContentType() == null ? 1 : plan.getContentType());
        order.setStatus(0); // 待支付
        mapper.insertOrder(order);
        mapper.insertOrderLog(order.getId(), buyerId, "CREATE_ORDER");
        return order;
    }

    @Override
    public List<CommissionOrder> getBuyerOrders(Long buyerId) {
        return mapper.getOrdersByBuyer(buyerId);
    }

    @Override
    public CommissionOrder getOrder(Long orderId) {
        return mapper.getOrderById(orderId);
    }

    @Override
    public CommissionOrder getOrderByNo(String orderNo) {
        return mapper.getOrderByNo(orderNo);
    }

    @Override
    public boolean paySuccess(String orderNo, String transactionId) {
        CommissionOrder order = mapper.getOrderByNo(orderNo);
        if (order == null) {
            return false;
        }
        // 幂等：已经支付过就直接返回成功
        if (order.getStatus() != null && order.getStatus() >= 1) {
            return true;
        }
        mapper.updateOrderStatusByOrderNo(orderNo, 1); // 绘制中（资金托管）
        mapper.updateTransactionId(orderNo, transactionId);

        Transaction pay = new Transaction();
        pay.setOrderNo(orderNo);
        pay.setBuyerId(order.getBuyerId());
        pay.setArtistId(order.getArtistId());
        pay.setAmount(order.getAmount());
        pay.setType("PAY");
        pay.setTransactionId(transactionId);
        mapper.insertTransaction(pay);

        mapper.insertOrderLogByOrderNo(orderNo, order.getBuyerId(), "PAY_SUCCESS");
        return true;
    }

    @Override
    public boolean confirmOrder(Long buyerId, Long orderId) {
        CommissionOrder order = mapper.getOrderById(orderId);
        if (order == null || !buyerId.equals(order.getBuyerId())) {
            return false;
        }
        if (order.getStatus() == null || order.getStatus() != 2) {
            return false; // 只有「待验收」可以确认
        }
        mapper.updateOrderStatus(orderId, 3); // 已完成
        mapper.insertOrderLog(orderId, buyerId, "CONFIRM_COMPLETE");

        Transaction income = new Transaction();
        income.setOrderNo(order.getOrderNo());
        income.setBuyerId(order.getBuyerId());
        income.setArtistId(order.getArtistId());
        income.setAmount(order.getAmount());
        income.setType("INCOME");
        income.setTransactionId(order.getTransactionId());
        mapper.insertTransaction(income);
        return true;
    }

    @Override
    public boolean refundOrder(String orderNo, Long operatorId) {
        CommissionOrder order = mapper.getOrderByNo(orderNo);
        if (order == null || order.getStatus() == null) {
            return false;
        }
        // 只有「绘制中」(已付款、未交付) 才允许退款；待支付无需退款，已交付走验收
        if (order.getStatus() != 1) {
            return false;
        }
        mapper.updateOrderStatusByOrderNo(orderNo, 4); // 4 = 已退款
        mapper.insertOrderLogByOrderNo(orderNo,
                operatorId == null ? order.getBuyerId() : operatorId, "REFUND_SUCCESS");

        Transaction refund = new Transaction();
        refund.setOrderNo(orderNo);
        refund.setBuyerId(order.getBuyerId());
        refund.setArtistId(order.getArtistId());
        refund.setAmount(order.getAmount());
        refund.setType("REFUND");
        refund.setTransactionId(order.getTransactionId());
        mapper.insertTransaction(refund);
        return true;
    }

    private String generateOrderNo() {
        long ts = System.currentTimeMillis();
        int rnd = ThreadLocalRandom.current().nextInt(1000, 9999);
        return "C" + ts + rnd;
    }
}
