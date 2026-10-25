package com.dimension.controller.admin;

import cn.hutool.core.map.MapUtil;
import com.dimension.service.UserService;
import com.dimension.service.impl.UserServiceImpl;
import com.dimension.utils.JsonResultOutputUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "AuditArtist", value = "/AuditArtist")
public class AuditArtistServlet extends HttpServlet {
    private UserService userService = new UserServiceImpl();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long userId = Long.valueOf(req.getParameter("userId"));
        Integer status = Integer.valueOf(req.getParameter("status")); // 2-通过, 3-驳回

        userService.auditArtist(userId, status);

        JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder().put("flag", 0).build());
    }
}