package com.dimension.controller.image;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "ImageCommentsDecrement", value = "/ImageCommentsDecrement")
public class ImageCommentsDecrement extends BaseImageServlet{
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long imageId = Long.valueOf(req.getParameter("imageId"));
        imageService.imageCommentsDecrement(imageId);
    }
}
