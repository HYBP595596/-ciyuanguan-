package com.dimension.controller.user;

import cn.hutool.core.map.MapUtil;
import com.dimension.entity.User;
import com.dimension.utils.JsonResultOutputUtil;
import com.dimension.utils.PasswordUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

/**
 * 用户自助注销账号（POST /CancelAccount?userId=&password=）。
 *
 * 采用「软注销」而不是物理删除：
 *   - status 置为 2（0 正常 / 1 禁用 / 2 已注销），登录与鉴权都会拒绝该账号
 *   - 密码替换成随机哈希，旧密码立即失效，管理员也无法用原密码登录该账号
 *   - 昵称改为“已注销用户{id}”，简介/头像/背景清空，画师身份与接稿开关关闭
 *   - 作品、评论、订单等历史数据保留，避免破坏他人记录（展示为已注销用户）
 *
 * 注销前必须输入正确密码做二次确认，防止 token 被盗后被一键注销。
 */
@WebServlet(name = "CancelAccount", value = "/CancelAccount")
public class CancelAccount extends BaseUserServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long userId;
        try {
            userId = Long.parseLong(req.getParameter("userId"));
        } catch (Exception e) {
            out(resp, 1, "参数错误：userId 无效");
            return;
        }

        String password = req.getParameter("password");
        if (password == null || password.trim().isEmpty()) {
            out(resp, 1, "请输入登录密码后再注销");
            return;
        }

        User user = userService.getUserInfo(userId);
        if (user == null) {
            out(resp, 1, "账号不存在");
            return;
        }
        if (user.getStatus() != null && user.getStatus() == 2) {
            out(resp, 1, "该账号已注销");
            return;
        }
        if (user.getStatus() != null && user.getStatus() == 1) {
            out(resp, 1, "该账号已被禁用，无需注销");
            return;
        }
        // 兼容历史明文密码（PasswordUtil.matches 内部会判断）
        if (!PasswordUtil.matches(password, user.getPassword())) {
            out(resp, 1, "密码错误，无法注销账号");
            return;
        }

        String maskedPassword = PasswordUtil.hash(UUID.randomUUID().toString());
        String maskedNickname = "已注销用户" + userId;
        int rows = userService.cancelAccount(userId, maskedPassword, maskedNickname);

        if (rows > 0) {
            JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                    .put("flag", 0)
                    .put("message", "账号已注销")
                    .build());
        } else {
            out(resp, 1, "注销失败：账号状态已发生变化，请刷新后重试");
        }
    }

    private void out(HttpServletResponse resp, int flag, String message) throws IOException {
        JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                .put("flag", flag)
                .put("message", message)
                .build());
    }
}
