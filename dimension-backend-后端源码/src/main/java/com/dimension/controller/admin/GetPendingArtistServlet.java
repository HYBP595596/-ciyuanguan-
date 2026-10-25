package com.dimension.controller.admin;

import cn.hutool.core.map.MapUtil;
import com.dimension.entity.User;
import com.dimension.service.UserService;
import com.dimension.service.impl.UserServiceImpl;
import com.dimension.utils.JsonResultOutputUtil;
import com.dimension.utils.PageUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "GetPendingArtist", value = "/GetPendingArtist")
public class GetPendingArtistServlet extends HttpServlet {
    private UserService userService = new UserServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer pageNum = PageUtil.getPageNum(req);
        Integer pageSize = PageUtil.getPageSize(req);

        List<User> list = userService.getPendingArtists(pageNum, pageSize);
        long total = userService.getPendingArtistTotal();

        JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                .put("artistList", list)
                .put("total", total)
                .build());
    }
}