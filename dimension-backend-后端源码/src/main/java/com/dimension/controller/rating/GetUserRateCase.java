package com.dimension.controller.rating;

import cn.hutool.core.map.MapUtil;
import com.dimension.entity.Admin;
import com.dimension.entity.ImageRating;
import com.dimension.service.ImageRatingService;
import com.dimension.utils.JsonResultOutputUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "GetUserRateCase", value = "/GetUserRateCase")
public class GetUserRateCase extends BaseImageRatingServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long userId = Long.valueOf(req.getParameter("userId"));
        Long imageId = Long.valueOf(req.getParameter("imageId"));

        ImageRating imageRating = imageRatingService.getUserRate(userId, imageId);

        JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                .put("imageRating", imageRating).build());
    }
}
