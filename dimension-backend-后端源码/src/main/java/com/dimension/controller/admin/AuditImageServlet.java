package com.dimension.controller.admin;

import com.dimension.controller.image.BaseImageServlet;

import cn.hutool.core.map.MapUtil;
import com.dimension.utils.JsonResultOutputUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "AuditImage", value = "/AuditImage")
public class AuditImageServlet extends BaseImageServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long imageId = Long.valueOf(req.getParameter("imageId"));
        Integer status = Integer.valueOf(req.getParameter("status")); // 1-通过 2-驳回

        imageService.auditImage(imageId, status);

        JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                .put("flag", 0).build());
    }
}