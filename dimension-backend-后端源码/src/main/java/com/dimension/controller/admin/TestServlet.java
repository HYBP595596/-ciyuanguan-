package com.dimension.controller.admin;

import cn.hutool.core.map.MapUtil;
import com.dimension.utils.JsonResultOutputUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "Test", value = "/")
public class TestServlet extends BaseAdminServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                .put("result", "响应成功").build());
    }
}
