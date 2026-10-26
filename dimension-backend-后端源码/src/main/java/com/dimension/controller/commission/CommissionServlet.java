package com.dimension.controller.commission;

import cn.hutool.core.map.MapUtil;
import com.dimension.entity.CommissionOrder;
import com.dimension.entity.CommissionPlan;
import com.dimension.entity.User;
import com.dimension.service.CommissionService;
import com.dimension.service.impl.CommissionServiceImpl;
import com.dimension.utils.JsonResultOutputUtil;
import com.dimension.utils.PageUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 约稿：画师广场 / 画师方案 / 下单 / 我的约稿 / 验收
 * 统一入口 /Commission?action=xxx
 */
@WebServlet(name = "Commission", value = "/Commission")
public class CommissionServlet extends HttpServlet {

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
                    } else {
                        JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                                .put("flag", 0)
                                .put("orderId", order.getId())
                                .put("orderNo", order.getOrderNo())
                                .put("amount", order.getAmount())
                                .build());
                    }
                    return;
                }
                case "myOrders": {
                    Long buyerId = Long.valueOf(req.getParameter("buyerId"));
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
                    boolean ok = commissionService.confirmOrder(buyerId, orderId);
                    JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                            .put("flag", ok ? 0 : 1)
                            .put("message", ok ? "已确认完成，款项已结算给画师" : "确认失败：订单不在待验收状态")
                            .build());
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
}
