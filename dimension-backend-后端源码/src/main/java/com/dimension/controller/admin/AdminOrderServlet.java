package com.dimension.controller.admin;

import cn.hutool.core.map.MapUtil;
import com.dimension.entity.Admin;
import com.dimension.entity.CommissionOrder;
import com.dimension.entity.Transaction;
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
import java.util.List;

/**
 * 管理端订单管理 /AdminOrder?action=xxx（必须带管理员 token）
 *
 *   list         订单列表（keyword 可按订单号/买家/画师搜，status 过滤）
 *   detail       订单详情（含流水）
 *   stats        按状态统计 + 成交额/抽成
 *   forceRefund  强制退款（允许「待验收」状态）
 *   forceClose   强制关闭待支付订单
 *   closeExpired 立刻执行一次超时关单
 */
@WebServlet(name = "AdminOrder", value = "/AdminOrder")
public class AdminOrderServlet extends HttpServlet {

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
        Admin admin = currentAdmin(req);
        if (admin == null) {
            resp.setStatus(401);
            JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                    .put("flag", 1).put("needLogin", true)
                    .put("message", "管理员未登录或登录已过期").build());
            return;
        }
        try {
            switch (action) {
                case "list": {
                    Integer status = req.getParameter("status") == null || req.getParameter("status").isEmpty()
                            ? null : Integer.valueOf(req.getParameter("status"));
                    String keyword = req.getParameter("keyword");
                    Integer pageNum = PageUtil.getPageNum(req);
                    Integer pageSize = PageUtil.getPageSize(req);
                    List<CommissionOrder> list = commissionService
                            .getAdminOrders(keyword, status, pageNum, pageSize);
                    JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                            .put("flag", 0)
                            .put("orderList", list)
                            .put("total", commissionService.getAdminOrderTotal(keyword, status))
                            .put("stats", commissionService.getOrderStats())
                            .build());
                    return;
                }
                case "detail": {
                    String orderNo = req.getParameter("orderNo");
                    Long orderId = req.getParameter("orderId") == null || req.getParameter("orderId").isEmpty()
                            ? null : Long.valueOf(req.getParameter("orderId"));
                    CommissionOrder order = orderNo != null && !orderNo.isEmpty()
                            ? commissionService.getOrderByNo(orderNo)
                            : (orderId == null ? null : commissionService.getOrder(orderId));
                    if (order == null) {
                        JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                                .put("flag", 1).put("message", "订单不存在").build());
                        return;
                    }
                    List<Transaction> transactions =
                            commissionService.getTransactions(order.getOrderNo());
                    JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                            .put("flag", 0)
                            .put("order", order)
                            .put("transactions", transactions)
                            .build());
                    return;
                }
                case "stats": {
                    JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                            .put("flag", 0)
                            .put("stats", commissionService.getOrderStats())
                            .build());
                    return;
                }
                case "forceRefund": {
                    String orderNo = req.getParameter("orderNo");
                    String reason = req.getParameter("reason");
                    boolean ok = paymentService.refundByOrder(orderNo,
                            reason == null ? "管理员强制退款" : reason, adminId(admin));
                    JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                            .put("flag", ok ? 0 : 1)
                            .put("message", ok ? "已退款并关闭订单" : "退款失败：订单未支付或状态已不允许退款")
                            .build());
                    return;
                }
                case "forceClose": {
                    Long orderId = Long.valueOf(req.getParameter("orderId"));
                    boolean ok = commissionService.closeOrderByAdmin(orderId, adminId(admin));
                    JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                            .put("flag", ok ? 0 : 1)
                            .put("message", ok ? "订单已关闭" : "关闭失败：只有待支付订单可以关闭")
                            .build());
                    return;
                }
                case "closeExpired": {
                    int closed = commissionService.closeExpiredOrders();
                    JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                            .put("flag", 0)
                            .put("closed", closed)
                            .put("message", "本次关闭超时订单 " + closed + " 笔")
                            .build());
                    return;
                }
                default:
                    JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                            .put("flag", 1).put("message", "未知操作: " + action).build());
            }
        } catch (NumberFormatException e) {
            JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                    .put("flag", 1).put("message", "参数格式不正确").build());
        } catch (Exception e) {
            JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                    .put("flag", 1).put("message", "操作失败: " + e.getMessage()).build());
        }
    }

    private Admin currentAdmin(HttpServletRequest req) {
        Object attr = req.getAttribute("currentAdmin");
        if (attr instanceof Admin) {
            return (Admin) attr;
        }
        return AdminAuthUtil.resolve(AdminAuthUtil.extractToken(req));
    }

    /** Admin.id 是 Integer，服务层统一用 Long */
    private static Long adminId(Admin admin) {
        return admin == null || admin.getId() == null ? null : admin.getId().longValue();
    }
}
