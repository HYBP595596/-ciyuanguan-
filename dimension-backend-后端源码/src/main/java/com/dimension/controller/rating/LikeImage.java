package com.dimension.controller.rating;

import com.dimension.service.ImageRatingService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "LikeImage", value = "/LikeImage")
public class LikeImage extends BaseImageRatingServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long userId = Long.valueOf(req.getParameter("userId"));
        Long imageId = Long.valueOf(req.getParameter("imageId"));

        imageRatingService.setLike(userId, imageId);
    }
}
