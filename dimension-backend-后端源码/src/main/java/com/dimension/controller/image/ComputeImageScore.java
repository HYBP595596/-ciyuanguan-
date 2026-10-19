package com.dimension.controller.image;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

//接口3.15 小鹏
@WebServlet(name = "ComputeImageScore", value = "/ComputeImageScore")
public class ComputeImageScore extends BaseImageServlet{
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long imageId = Long.parseLong(req.getParameter("imageId"));
        Integer likes = Integer.parseInt(req.getParameter("likes"));
        Integer dislikes = Integer.parseInt(req.getParameter("dislikes"));

        imageService.computeImageScore(imageId,likes,dislikes);
    }
}
