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
                    if (title == null || title.trim().isEmpty()) {
                        out(resp, MapUtil.builder().put("flag", 1).put("message", "请填写方案标题").build());
                        return;
                    }
                    BigDecimal price = new BigDecimal(req.getParameter("price"));
                    if (price.compareTo(BigDecimal.ZERO) <= 0) {
                        out(resp, MapUtil.builder().put("flag", 1).put("message", "价格必须大于 0").build());
                        return;
                    }
                    Integer days = Integer.valueOf(req.getParameter("deliveryDays"));
                    if (days <= 0) {
                        out(resp, MapUtil.builder().put("flag", 1).put("message", "交稿天数必须大于 0").build());
                        return;
                    }
                    String cover = req.getParameter("coverUrl");
                    // 约稿类型：1插画 2漫画 3小说
                    Integer contentType = intParam(req, "contentType", 1);
                    // 0草稿 1上架，默认直接上架
                    Integer status = intParam(req, "status", 1);
                    if (status != 0 && status != 1) {
                        status = 1;
                    }
                    CommissionPlan plan = commissionService.addPlan(artistId, title.trim(), price, days, cover, contentType, status);
                    if (plan == null) {
                        out(resp, MapUtil.builder().put("flag", 1).put("message", "需要先通过画师认证").build());
                    } else {
                        out(resp, MapUtil.builder()
                                .put("flag", 0)
                                .put("planId", plan.getId())
                                .put("status", plan.getStatus())
                                .build());
                    }
                    return;
                }
                case "planEdit": {
                    Long artistId = Long.valueOf(req.getParameter("artistId"));
                    Long planId = Long.valueOf(req.getParameter("planId"));
                    String title = req.getParameter("title");
                    if (title == null || title.trim().isEmpty()) {
                        out(resp, MapUtil.builder().put("flag", 1).put("message", "请填写方案标题").build());
                        return;
                    }
                    BigDecimal price = new BigDecimal(req.getParameter("price"));
                    if (price.compareTo(BigDecimal.ZERO) <= 0) {
                        out(resp, MapUtil.builder().put("flag", 1).put("message", "价格必须大于 0").build());
                        return;
                    }
                    Integer days = Integer.valueOf(req.getParameter("deliveryDays"));
                    if (days <= 0) {
                        out(resp, MapUtil.builder().put("flag", 1).put("message", "交稿天数必须大于 0").build());
                        return;
                    }
                    String cover = req.getParameter("coverUrl");
                    Integer contentType = intParam(req, "contentType", 1);
                    Integer status = intParam(req, "status", 1);
                    // 只允许 0草稿 / 1上架 / 2下架
                    if (status != 0 && status != 1 && status != 2) {
                        status = 2;
                    }
                    int rows = commissionService.updatePlan(artistId, planId, title.trim(), price,
                            days, cover, contentType, status);
                    out(resp, MapUtil.builder()
                            .put("flag", rows > 0 ? 0 : 1)
                            .put("message", rows > 0 ? "已保存" : "方案不存在或不属于你")
                            .build());
                    return;
                }
                case "planDelete": {
                    Long artistId = Long.valueOf(req.getParameter("artistId"));
                    Long planId = Long.valueOf(req.getParameter("planId"));
                    int rows = commissionService.deletePlan(artistId, planId);
                    if (rows > 0) {
                        out(resp, MapUtil.builder().put("flag", 0).put("message", "方案已删除").build());
                    } else if (rows < 0) {
                        out(resp, MapUtil.builder()
                                .put("flag", 1)
                                .put("message", "该方案已经有订单，不能删除；请改为「下架」，历史订单仍需能查到方案")
                                .build());
                    } else {
                        out(resp, MapUtil.builder().put("flag", 1).put("message", "方案不存在或不属于你").build());
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

    /** 读取 int 参数，缺省/空串时用默认值 */
    private int intParam(HttpServletRequest req, String name, int defaultValue) {
        String raw = req.getParameter(name);
        if (raw == null || raw.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
