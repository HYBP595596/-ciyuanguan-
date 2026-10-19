package com.dimension.controller.imageRating;

import cn.hutool.core.map.MapUtil;
import com.dimension.utils.JsonResultOutputUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "GetImageLikes", value = "/GetImageLikes")
public class GetImageLikes extends BaseImageRatingServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long imageId = Long.valueOf(req.getParameter("imageId"));

        Long likes = imageRatingService.getLikes(imageId);

        JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                .put("likes", likes).build());
    }
}
