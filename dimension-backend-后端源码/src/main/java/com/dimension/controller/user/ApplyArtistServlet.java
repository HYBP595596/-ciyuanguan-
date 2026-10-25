package com.dimension.controller.user;

import cn.hutool.core.map.MapUtil;
import com.dimension.utils.JsonResultOutputUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "ApplyArtist", value = "/ApplyArtist")
public class ApplyArtistServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long userId = Long.valueOf(req.getParameter("userId"));
        String desc = req.getParameter("desc");

        com.dimension.service.UserService userService = new com.dimension.service.impl.UserServiceImpl();
        userService.applyArtist(userId, desc);

        JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                .put("flag", 0)
                .put("message", "申请已提交，请等待管理员审核")
                .build());
    }
}