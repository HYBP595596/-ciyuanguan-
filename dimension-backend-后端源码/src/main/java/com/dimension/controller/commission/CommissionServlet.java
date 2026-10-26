package com.dimension.controller.commission;

import cn.hutool.core.map.MapUtil;
import com.dimension.config.PlatformConfig;
import com.dimension.entity.Admin;
import com.dimension.entity.CommissionOrder;
import com.dimension.entity.CommissionPlan;
import com.dimension.entity.Payment;
import com.dimension.entity.User;
import com.dimension.payment.PaymentGatewayFactory;
import com.dimension.service.CommissionService;
import com.dimension.service.PaymentService;
import com.dimension.service.impl.CommissionServiceImpl;
import com.dimension.service.impl.PaymentServiceImpl;
import com.dimension.utils.AdminAuthUtil;
import com.dimension.utils.JsonResultOutputUtil;
import com.dimension.utils.PageUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 约稿：画师广场 / 画师方案 / 下单 / 我的约稿 / 验收 / 取消
 * 统一入口 /Commission?action=xxx
 *
 * 支付不在这里另起一套：下单后直接用 PaymentService 建支付单并返回支付参数，
 * 后续「支付回调 → 订单流转」仍由 /PayNotify + PaymentService 驱动。
 */
@WebServlet(name = "Commission", value = "/Commission")
public class CommissionServlet extends HttpServlet {

    private final CommissionService commissionService = new CommissionServiceImpl();
    private final PaymentService paymentService = new PaymentServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handle(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handle(req, resp);
    }

    private void handle(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String action = req.getParameter("action");
        if (action == null) {
            action = "";
        }
        try {
            switch (action) {
                case "artists": {
                    Integer pageNum = PageUtil.getPageNum(req);
                    Integer pageSize = PageUtil.getPageSize(req);
                    List<User> list = commissionService.getAcceptingArtists(pageNum, pageSize);
                    // 不向前端暴露密码
                    for (User u : list) {
                        u.setPassword(null);
                    }
                    JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                            .put("artistList", list)
                            .put("total", commissionService.getAcceptingArtistTotal())
                            .build());
                    return;
                }
                case "artistPlans": {
                    Long artistId = Long.valueOf(req.getParameter("artistId"));
                    List<CommissionPlan> list = commissionService.getArtistOnlinePlans(artistId);
                    User artist = commissionService.getArtist(artistId);
                    if (artist != null) {
                        artist.setPassword(null);
                    }
                    JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                            .put("planList", list)
                            .put("artist", artist)
                            .build());
                    return;
                }
                case "onlinePlans": {
                    Integer pageNum = PageUtil.getPageNum(req);
                    Integer pageSize = PageUtil.getPageSize(req);
                    List<CommissionPlan> list = commissionService.getOnlinePlans(pageNum, pageSize);
                    JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                            .put("planList", list)
                            .put("total", commissionService.getOnlinePlanTotal())
                            .build());
                    return;
                }
                case "channels": {
                    // 前端据此只显示真正可用的支付方式
                    JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                            .put("flag", 0)
                            .put("channels", PaymentGatewayFactory.availability())
                            .build());
                    return;
                }
                case "create": {
                    Long buyerId = Long.valueOf(req.getParameter("buyerId"));
                    Long planId = Long.valueOf(req.getParameter("planId"));
                    String requirement = req.getParameter("requirement");
                    if (requirement == null || requirement.trim().isEmpty()) {
                        JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                                .put("flag", 1).put("message", "请填写约稿需求").build());
                        return;
                    }
                    CommissionOrder order = commissionService.createOrder(buyerId, planId, requirement.trim());
                    if (order == null) {
                        JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                                .put("flag", 1).put("message", "方案不可用，或不能给自己的方案下单").build());
                        return;
                    }
                    // 下单即出支付参数：不再让前端多走一次 /Payment?action=create
                    String channel = req.getParameter("channel");
                    Payment payment = paymentService.createPayment(buyerId, order.getOrderNo(), channel);
                    Map<String, Object> payParams = payment == null
                            ? null : paymentService.payParams(payment);

                    Map<String, Object> result = new LinkedHashMap<>();
                    result.put("flag", 0);
                    result.put("orderId", order.getId());
                    result.put("orderNo", order.getOrderNo());
                    result.put("amount", order.getAmount());
                    result.put("expireMinutes", PlatformConfig.orderTimeoutMinutes());
                    if (payment == null) {
                        result.put("payReady", false);
                        result.put("payMessage", "订单已创建，但当前支付渠道不可用，请稍后在「我的约稿」里继续支付");
                    } else {
                        result.put("payReady", true);
                        result.put("paymentNo", payment.getPaymentNo());
                        result.put("channel", payment.getChannel());
                        result.put("payParams", payParams);
                    }
                    JsonResultOutputUtil.outputJsonResult(resp, result);
                    return;
                }
                case "myOrders": {
                    Long buyerId = Long.valueOf(req.getParameter("buyerId"));
                    // 惰性关单：进列表前先关掉超时未支付的，用户看到的状态才准
                    commissionService.closeExpiredOrders();
                    List<CommissionOrder> list = commissionService.getBuyerOrders(buyerId);
                    JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                            .put("orderList", list).build());
                    return;
                }
                case "detail": {
                    Long orderId = Long.valueOf(req.getParameter("orderId"));
                    CommissionOrder order = commissionService.getOrder(orderId);
                    JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                            .put("flag", order == null ? 1 : 0)
                            .put("order", order).build());
                    return;
                }
                case "confirm": {
                    Long buyerId = Long.valueOf(req.getParameter("buyerId"));
                    Long orderId = Long.valueOf(req.getParameter("orderId"));
                    Map<String, Object> settlement =
                            commissionService.confirmOrderWithSettlement(buyerId, orderId);
                    if (settlement == null) {
                        JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                                .put("flag", 1)
                                .put("message", "确认失败：订单不在待验收状态").build());
                        return;
                    }
                    JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                            .put("flag", 0)
                            .put("amount", settlement.get("amount"))
                            .put("platformFee", settlement.get("platformFee"))
                            .put("artistIncome", settlement.get("artistIncome"))
                            .put("message", "已确认完成，画师实收 " + settlement.get("artistIncome")
                                    + " 元（平台服务费 " + settlement.get("platformFee") + " 元）")
                            .build());
                    return;
                }
                case "cancel": {
                    Long buyerId = Long.valueOf(req.getParameter("buyerId"));
                    Long orderId = Long.valueOf(req.getParameter("orderId"));
                    boolean ok = commissionService.cancelOrder(buyerId, orderId);
                    JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                            .put("flag", ok ? 0 : 1)
                            .put("message", ok ? "订单已取消" : "取消失败：只有待支付的订单可以取消")
                            .build());
                    return;
                }
                case "closeExpired": {
                    // 定时任务之外的兜底入口（管理端也能点一下）
                    Admin admin = currentAdmin(req);
                    if (admin == null) {
                        resp.setStatus(401);
                        JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                                .put("flag", 1).put("needAdmin", true)
                                .put("message", "需要管理员权限").build());
                        return;
                    }
                    int closed = commissionService.closeExpiredOrders();
                    JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                            .put("flag", 0).put("closed", closed)
                            .put("message", "本次关闭超时订单 " + closed + " 笔").build());
                    return;
                }
                default:
                    JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                            .put("flag", 1).put("message", "未知操作: " + action).build());
            }
        } catch (Exception e) {
            JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                    .put("flag", 1).put("message", "参数错误: " + e.getMessage()).build());
        }
    }

    private Admin currentAdmin(HttpServletRequest req) {
        Object attr = req.getAttribute("currentAdmin");
        if (attr instanceof Admin) {
            return (Admin) attr;
        }
        return AdminAuthUtil.resolve(AdminAuthUtil.extractToken(req));
    }
}
