package com.dimension.controller.rating;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "CancelRateImage", value = "/CancelRateImage")
public class CancelRateImage extends BaseImageRatingServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long userId = Long.valueOf(req.getParameter("userId"));
        Long imageId = Long.valueOf(req.getParameter("imageId"));


        imageRatingService.setCancelImageRate(userId, imageId);
    }
}
