package com.dimension.controller.image;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
@WebServlet(name = "ImageViewsIncrement", value = "/ImageViewsIncrement")
public class ImageViewsIncrement extends BaseImageServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        long imageId = Long.parseLong(req.getParameter("imageId"));

        imageService.increaseImageViewsIncrement(imageId);

    }
}
