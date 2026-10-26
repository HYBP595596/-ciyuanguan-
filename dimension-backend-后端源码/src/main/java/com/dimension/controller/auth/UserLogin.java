package com.dimension.controller.auth;

import com.dimension.controller.user.BaseUserServlet;

import cn.hutool.core.map.MapUtil;
import com.dimension.utils.JsonResultOutputUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@WebServlet(name = "UserLogin", value = "/UserLogin")
public class UserLogin extends BaseUserServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");

        Map<String, Object> result = userService.userLogin(username, password, resp);

        JsonResultOutputUtil.outputJsonResult(resp, result);
    }
}
