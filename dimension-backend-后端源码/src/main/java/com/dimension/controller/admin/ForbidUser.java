package com.dimension.controller.admin;

import com.dimension.controller.user.BaseUserServlet;


import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@WebServlet(name = "ForbidUser",value = "/ForbidUser")
public class ForbidUser extends BaseUserServlet{
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer userId = Integer.parseInt(req.getParameter("userId"));

        userService.forbidUser(userId);
    }
}
