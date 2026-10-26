package com.dimension.controller.admin;

import cn.hutool.core.map.MapUtil;
import com.dimension.entity.Admin;
import com.dimension.utils.AdminAuthUtil;
import com.dimension.utils.JsonResultOutputUtil;
import com.dimension.utils.JwtUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 管理员账号管理 /AdminAccount?action=xxx
 *
 * 鉴权双重保险：
 *   1. AdminAuthenticationFilter 已对 /AdminAccount 做统一拦截
 *   2. 这里再用 AdminAuthUtil 解析一次（含 token 版本校验），拿到操作人身份
 */
@WebServlet(name = "AdminAccount", value = "/AdminAccount")
public class AdminAccountServlet extends BaseAdminServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handle(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handle(req, resp);
    }

    private void handle(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Admin current = (Admin) req.getAttribute("currentAdmin");
        if (current == null) {
            current = AdminAuthUtil.resolve(AdminAuthUtil.extractToken(req));
        }
        if (current == null) {
            JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                    .put("flag", 1)
                    .put("needLogin", true)
                    .put("message", "管理员未登录或登录已过期").build());
            return;
        }
        Integer operatorId = current.getId();

        String action = req.getParameter("action");
        if (action == null) action = "";
        try {
            switch (action) {
                case "list": {
                    int pageNum = intParam(req, "pageNum", 0);
                    int pageSize = intParam(req, "pageSize", 10);
                    List<Admin> list = adminService.getAdminList(pageNum, pageSize);
                    JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                            .put("adminList", list)
                            .put("total", adminService.getAdminTotal())
                            .put("operatorId", operatorId)
                            .build());
                    return;
                }
                case "create": {
                    String err = adminService.createAdmin(
                            req.getParameter("username"),
                            req.getParameter("nickname"),
                            req.getParameter("password"));
                    out(resp, err);
                    return;
                }
                case "update": {
                    Integer id = Integer.valueOf(req.getParameter("id"));
                    String err = adminService.updateAdmin(id,
                            req.getParameter("username"), req.getParameter("nickname"));
                    out(resp, err);
                    return;
                }
                case "resetPassword": {
                    Integer id = Integer.valueOf(req.getParameter("id"));
                    String err = adminService.resetPassword(id, req.getParameter("password"));
                    out(resp, err);
                    return;
                }
                case "changeOwnPassword": {
                    Integer id = Integer.valueOf(req.getParameter("id"));
                    if (!id.equals(operatorId)) {
                        JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                                .put("flag", 1).put("message", "只能修改自己的密码").build());
                        return;
                    }
                    String err = adminService.changeOwnPassword(id,
                            req.getParameter("oldPassword"), req.getParameter("newPassword"));
                    if (err != null) {
                        out(resp, err);
                        return;
                    }
                    // 改密后旧 token 已失效，给当前操作者补发一个新 token，避免自己被踢下线
                    Admin fresh = adminService.getAdminById(id);
                    String newToken = JwtUtil.createToken(fresh);
                    JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                            .put("flag", 0)
                            .put("message", "密码已修改")
                            .put("adminToken", newToken)
                            .build());
                    return;
                }
                case "delete": {
                    Integer id = Integer.valueOf(req.getParameter("id"));
                    String err = adminService.deleteAdmin(id, operatorId);
                    out(resp, err);
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

    private void out(HttpServletResponse resp, String err) throws IOException {
        if (err == null) {
            JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                    .put("flag", 0).put("message", "操作成功").build());
        } else {
            JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                    .put("flag", 1).put("message", err).build());
        }
    }

    private int intParam(HttpServletRequest req, String name, int def) {
        String v = req.getParameter(name);
        if (v == null || v.trim().isEmpty()) return def;
        try {
            return Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }
}
