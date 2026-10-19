package com.dimension.controller.user;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

// 接口9.3--小徐
@WebServlet(name = "UpdateUserAvatar", value = "/UpdateUserAvatar")
public class UpdateUserAvatar extends BaseUserServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 获取请求参数 userId、avatar
        String avatar = req.getParameter("avatar");
        Long userId = null;
        try {
            userId = Long.parseLong(req.getParameter("userId"));
        } catch (NumberFormatException e) {
            System.err.println("字符串 " + userId + " 无法被解析为long ");
        }

        // 调用service写入数据
        int resUpdateAvatar = userService.updateUserAvatar(userId, avatar);
        System.out.println("resUpdateAvatar = " + resUpdateAvatar);
    }
}
