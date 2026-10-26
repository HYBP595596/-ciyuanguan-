package com.dimension.controller.commission;

import cn.hutool.core.map.MapUtil;
import com.dimension.entity.CommissionOrder;
import com.dimension.entity.CommissionPlan;
import com.dimension.service.CommissionService;
import com.dimension.service.impl.CommissionServiceImpl;
import com.dimension.utils.JsonResultOutputUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

/**
 * 画师中心：接稿开关 / 方案(档期)管理 / 收到的订单 / 交稿
 * 统一入口 /Artist?action=xxx
 */
@WebServlet(name = "ArtistCenter", value = "/Artist")
public class ArtistServlet extends HttpServlet {

    private final CommissionService commissionService = new CommissionServiceImpl();

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
                case "accept": {
                    Long userId = Long.valueOf(req.getParameter("userId"));
                    Integer accept = Integer.valueOf(req.getParameter("accept"));
                    int rows = commissionService.setAcceptCommission(userId, accept);
                    if (rows < 0) {
                        out(resp, MapUtil.builder().put("flag", 1).put("message", "需要先通过画师认证").build());
                    } else {
                        out(resp, MapUtil.builder().put("flag", 0).put("acceptCommission", accept).build());
                    }
                    return;
                }
                case "planAdd": {
                    Long artistId = Long.valueOf(req.getParameter("artistId"));
                    String title = req.getParameter("title");
                    BigDecimal price = new BigDecimal(req.getParameter("price"));
                    Integer days = Integer.valueOf(req.getParameter("deliveryDays"));
                    String cover = req.getParameter("coverUrl");
                    // 约稿类型：1插画 2漫画 3小说
                    String ct = req.getParameter("contentType");
                    Integer contentType = (ct == null || ct.trim().isEmpty()) ? 1 : Integer.valueOf(ct);
                    CommissionPlan plan = commissionService.addPlan(artistId, title, price, days, cover, contentType);
                    if (plan == null) {
                        out(resp, MapUtil.builder().put("flag", 1).put("message", "需要先通过画师认证").build());
                    } else {
                        out(resp, MapUtil.builder().put("flag", 0).put("planId", plan.getId()).build());
                    }
                    return;
                }
                case "planStatus": {
                    Long artistId = Long.valueOf(req.getParameter("artistId"));
                    Long planId = Long.valueOf(req.getParameter("planId"));
                    Integer status = Integer.valueOf(req.getParameter("status"));
                    int rows = commissionService.updatePlanStatus(artistId, planId, status);
                    out(resp, MapUtil.builder().put("flag", rows > 0 ? 0 : 1).build());
                    return;
                }
                case "plans": {
                    Long artistId = Long.valueOf(req.getParameter("artistId"));
                    List<CommissionPlan> list = commissionService.getMyPlans(artistId);
                    out(resp, MapUtil.builder().put("planList", list).build());
                    return;
                }
                case "orders": {
                    Long artistId = Long.valueOf(req.getParameter("artistId"));
                    List<CommissionOrder> list = commissionService.getArtistOrders(artistId);
                    out(resp, MapUtil.builder().put("orderList", list).build());
                    return;
                }
                case "deliver": {
                    Long artistId = Long.valueOf(req.getParameter("artistId"));
                    Long orderId = Long.valueOf(req.getParameter("orderId"));
                    String type = req.getParameter("type"); // preview / final
                    String url = req.getParameter("url");
                    boolean ok = commissionService.deliverOrder(artistId, orderId, type, url);
                    out(resp, MapUtil.builder()
                            .put("flag", ok ? 0 : 1)
                            .put("message", ok ? "已交付" : "交付失败：订单不属于你或不在绘制中")
                            .build());
                    return;
                }
                default:
                    out(resp, MapUtil.builder().put("flag", 1).put("message", "未知操作: " + action).build());
            }
        } catch (Exception e) {
            out(resp, MapUtil.builder().put("flag", 1).put("message", "参数错误: " + e.getMessage()).build());
        }
    }

    private void out(HttpServletResponse resp, java.util.Map<Object, Object> map) throws IOException {
        JsonResultOutputUtil.outputJsonResult(resp, map);
    }
}
