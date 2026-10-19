package com.dimension.controller.image;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

//接口3.13 小鹏
@WebServlet(name = "RemoveImageById", value = "/RemoveImageById")
public class RemoveImageById extends BaseImageServlet{
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //获取参数
        long imageId =  Long.parseLong(req.getParameter("imageId"));

        imageService.removeImageById(imageId);
    }
}
