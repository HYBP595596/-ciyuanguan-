package com.dimension.controller.user;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

// 接口9.5--小徐
@WebServlet(name = "UpdateUserPassword", value = "/UpdateUserPassword")
public class UpdateUserPassword extends BaseUserServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 获取请求参数 userId、password
        Long userId = null;
        try {
            userId = Long.parseLong(req.getParameter("userId"));
        } catch (NumberFormatException e) {
            System.err.println("字符串 " + userId + " 无法被解析为long ");
        }
        String password = req.getParameter("password");
        // 调用service写入数据
        int resUpdateUserPassword = userService.updateUserPassword(userId, password);
        System.out.println("resUpdateUserPassword = " + resUpdateUserPassword);

    }
}
