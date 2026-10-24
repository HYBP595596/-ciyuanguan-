package com.dimension.controller.user;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;

@WebServlet(name = "UpdateUserCover", value = "/UpdateUserCover")
public class UpdateUserCover extends BaseUserServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long userId = Long.parseLong(req.getParameter("userId"));
        // 原来的：String coverImage = req.getParameter("coverImage");
        // 改成：
        String coverImage = URLDecoder.decode(req.getParameter("coverImage"), "UTF-8");
        userService.updateUserCover(userId, coverImage);
    }
}