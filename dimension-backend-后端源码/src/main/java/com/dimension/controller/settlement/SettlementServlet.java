package com.dimension.controller.settlement;

import cn.hutool.core.map.MapUtil;
import com.dimension.entity.Admin;
import com.dimension.entity.ArtistBalance;
import com.dimension.entity.ArtistWithdraw;
import com.dimension.service.SettlementService;
import com.dimension.service.impl.SettlementServiceImpl;
import com.dimension.utils.AdminAuthUtil;
import com.dimension.utils.JsonResultOutputUtil;
import com.dimension.utils.PageUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 画师结算入口 /Settlement?action=xxx
 *
 *   balance      我的余额（画师本人；管理员可查任意人）
 *   apply        申请提现（画师本人）
 *   myWithdraws  我的提现记录
 *   withdraws    提现列表（管理员）
 *   audit        审核提现（管理员）：status=1 已打款 / 2 已驳回
 *
 * 注意：余额的钱只来自「订单验收」，这里只负责查询与提现流程，不参与订单状态流转。
 */
@WebServlet(name = "Settlement", value = "/Settlement")
public class SettlementServlet extends HttpServlet {

    private final SettlementService settlementService = new SettlementServiceImpl();

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
                case "balance": {
                    Long userId = Long.valueOf(req.getParameter("userId"));
                    ArtistBalance balance = settlementService.getBalance(userId);
                    long pending = settlementService.listMine(userId).stream()
                            .filter(w -> w.getStatus() != null && w.getStatus() == 0)
                            .count();
                    JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                            .put("flag", 0)
                            .put("balance", balance.getBalance())
                            .put("totalIncome", balance.getTotalIncome())
                            .put("totalWithdrawn", balance.getTotalWithdrawn())
                            .put("pendingWithdraws", pending)
                            .build());
                    return;
                }
                case "apply": {
                    Long userId = Long.valueOf(req.getParameter("userId"));
                    BigDecimal amount = new BigDecimal(req.getParameter("amount"));
                    String account = req.getParameter("account");
                    Map<String, Object> result = settlementService
                            .applyWithdraw(userId, amount, account == null ? null : account.trim());
                    JsonResultOutputUtil.outputJsonResult(resp, result);
                    return;
                }
                case "myWithdraws": {
                    Long userId = Long.valueOf(req.getParameter("userId"));
                    List<ArtistWithdraw> list = settlementService.listMine(userId);
                    JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                            .put("flag", 0)
                            .put("withdrawList", list)
                            .build());
                    return;
                }
                case "withdraws": {
                    if (currentAdmin(req) == null) {
                        outputNeedAdmin(resp);
                        return;
                    }
                    Integer status = req.getParameter("status") == null || req.getParameter("status").isEmpty()
                            ? null : Integer.valueOf(req.getParameter("status"));
                    Integer pageNum = PageUtil.getPageNum(req);
                    Integer pageSize = PageUtil.getPageSize(req);
                    List<ArtistWithdraw> list = settlementService.listAll(status, pageNum, pageSize);
                    JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                            .put("flag", 0)
                            .put("withdrawList", list)
                            .put("total", settlementService.countAll(status))
                            .build());
                    return;
                }
                case "audit": {
                    Admin admin = currentAdmin(req);
                    if (admin == null) {
                        outputNeedAdmin(resp);
                        return;
                    }
                    Long withdrawId = Long.valueOf(req.getParameter("withdrawId"));
                    Integer status = Integer.valueOf(req.getParameter("status"));
                    String remark = req.getParameter("remark");
                    boolean ok = settlementService.audit(withdrawId, status,
                            admin.getId() == null ? null : admin.getId().longValue(), remark);
                    JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                            .put("flag", ok ? 0 : 1)
                            .put("message", ok
                                    ? (status == 1 ? "已标记打款，余额扣减完成" : "已驳回，金额退回画师余额")
                                    : "审核失败：记录不存在或已被处理")
                            .build());
                    return;
                }
                default:
                    JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                            .put("flag", 1).put("message", "未知操作: " + action).build());
            }
        } catch (NumberFormatException e) {
            JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                    .put("flag", 1).put("message", "金额或参数格式不正确").build());
        } catch (Exception e) {
            JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                    .put("flag", 1).put("message", "参数错误: " + e.getMessage()).build());
        }
    }

    /** 过滤器已把管理员放进 currentAdmin；兜底再从 token 解析一次 */
    private Admin currentAdmin(HttpServletRequest req) {
        Object attr = req.getAttribute("currentAdmin");
        if (attr instanceof Admin) {
            return (Admin) attr;
        }
        return AdminAuthUtil.resolve(AdminAuthUtil.extractToken(req));
    }

    private void outputNeedAdmin(HttpServletResponse resp) throws IOException {
        resp.setStatus(401);
        JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                .put("flag", 1)
                .put("needLogin", true)
                .put("needAdmin", true)
                .put("message", "需要管理员权限")
                .build());
    }
}
