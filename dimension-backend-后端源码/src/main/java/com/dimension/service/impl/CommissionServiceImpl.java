package com.dimension.service.impl;

import com.dimension.config.PlatformConfig;
import com.dimension.entity.CommissionOrder;
import com.dimension.entity.CommissionPlan;
import com.dimension.entity.Transaction;
import com.dimension.entity.User;
import com.dimension.mapper.CommissionMapper;
import com.dimension.service.CommissionService;
import com.dimension.utils.SessionUtil;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

    // ==================== 状态流转（全部走事务） ====================

    @Override
    public boolean paySuccess(String orderNo, String transactionId) {
        return Boolean.TRUE.equals(SessionUtil.inTransaction(session ->
                CommissionTxOps.paySuccess(session, orderNo, transactionId)));
    }

    @Override
    public boolean confirmOrder(Long buyerId, Long orderId) {
        return confirmOrderWithSettlement(buyerId, orderId) != null;
    }

    @Override
    public Map<String, Object> confirmOrderWithSettlement(Long buyerId, Long orderId) {
        return SessionUtil.inTransaction(session -> {
            CommissionMapper cm = session.getMapper(CommissionMapper.class);
            CommissionOrder order = cm.getOrderByIdForUpdate(orderId);
            if (order == null || !buyerId.equals(order.getBuyerId())) {
                return null;
            }
            if (order.getStatus() == null || order.getStatus() != 2) {
                return null; // 只有「待验收」可以确认
            }
            BigDecimal[] settlement = CommissionTxOps.confirmComplete(session, order, buyerId);
            if (settlement == null) {
                return null;
            }
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("amount", settlement[0]);
            result.put("platformFee", settlement[1]);
            result.put("artistIncome", settlement[2]);
            return result;
        });
    }

    @Override
    public boolean refundOrder(String orderNo, Long operatorId) {
        return refund(orderNo, operatorId, false);
    }

    @Override
    public boolean refundOrderByAdmin(String orderNo, Long adminId) {
        return refund(orderNo, adminId, true);
    }

    private boolean refund(String orderNo, Long operatorId, boolean allowDelivered) {
        return Boolean.TRUE.equals(SessionUtil.inTransaction(session -> {
            CommissionMapper cm = session.getMapper(CommissionMapper.class);
            CommissionOrder order = cm.getOrderByNoForUpdate(orderNo);
            if (order == null) {
                return false;
            }
            return CommissionTxOps.refund(session, order, operatorId, allowDelivered);
        }));
    }

    @Override
    public boolean cancelOrder(Long buyerId, Long orderId) {
        return Boolean.TRUE.equals(SessionUtil.inTransaction(session -> {
            CommissionMapper cm = session.getMapper(CommissionMapper.class);
            CommissionOrder order = cm.getOrderByIdForUpdate(orderId);
            if (order == null || !buyerId.equals(order.getBuyerId())) {
                return false;
            }
            if (order.getStatus() == null || order.getStatus() != 0) {
                return false; // 只有待支付能取消
            }
            return CommissionTxOps.closeOrder(session, order, buyerId, "CANCEL_ORDER");
        }));
    }

    @Override
    public boolean closeOrderByAdmin(Long orderId, Long adminId) {
        return Boolean.TRUE.equals(SessionUtil.inTransaction(session -> {
            CommissionMapper cm = session.getMapper(CommissionMapper.class);
            CommissionOrder order = cm.getOrderByIdForUpdate(orderId);
            if (order == null || order.getStatus() == null || order.getStatus() != 0) {
                return false;
            }
            return CommissionTxOps.closeOrder(session, order, adminId, "ADMIN_CLOSE");
        }));
    }

    @Override
    public int closeExpiredOrders() {
        Integer closed = SessionUtil.inTransaction(session ->
                CommissionTxOps.closeExpired(session, PlatformConfig.orderTimeoutMinutes(), 200));
        return closed == null ? 0 : closed;
    }

    // ==================== 管理端 ====================

    @Override
    public List<CommissionOrder> getAdminOrders(String keyword, Integer status,
                                                Integer pageNum, Integer pageSize) {
        return mapper.getAdminOrders(keyword, status, pageNum, pageSize);
    }

    @Override
    public long getAdminOrderTotal(String keyword, Integer status) {
        return mapper.getAdminOrderTotal(keyword, status);
    }

    @Override
    public Map<String, Object> getOrderStats() {
        return mapper.getOrderStats();
    }

    @Override
    public List<Transaction> getTransactions(String orderNo) {
        return mapper.getTransactionsByOrderNo(orderNo);
    }

    private String generateOrderNo() {
        long ts = System.currentTimeMillis();
        int rnd = ThreadLocalRandom.current().nextInt(1000, 9999);
        return "C" + ts + rnd;
    }
}
