package com.dimension.controller.rating;

import cn.hutool.core.map.MapUtil;
import com.dimension.utils.JsonResultOutputUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "GetImageDislikes", value = "/GetImageDislikes")
public class GetImageDislikes extends BaseImageRatingServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long imageId = Long.valueOf(req.getParameter("imageId"));

        Long dislikes = imageRatingService.getDislikes(imageId);

        JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                .put("dislikes", dislikes).build());
    }
}
