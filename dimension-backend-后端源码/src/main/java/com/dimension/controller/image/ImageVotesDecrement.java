package com.dimension.controller.image;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

//接口3.8 小鹏
@WebServlet(name = "ImageVotesDecrement",value = "/ImageVotesDecrement")
public class ImageVotesDecrement extends BaseImageServlet{

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        long imageId = Long.parseLong(req.getParameter("imageId"));

        imageService.decreaseImageVotesByImageId(imageId);
    }
}
