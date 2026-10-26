package com.dimension.controller.bookmark;

import com.dimension.service.ImageBookmarkService;
import com.dimension.service.impl.ImageBookmarkServiceImpl;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@WebServlet(name = "AddImageBookmark", value = "/AddImageBookmark")
public class AddImageBookmarkServlet extends BaseImageBookmarkServlet{
    //4.2  AddImageBookmark => 添加收藏

    protected ImageBookmarkService imageBookmarkService = new ImageBookmarkServiceImpl();
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //获取请求参数
        Long userId = Long.parseLong(req.getParameter("userId"));
        Long imageId = Long.parseLong(req.getParameter("imageId"));
        //调用service插入数据
        //完成添加
        imageBookmarkService.insertImageBookmark(userId,imageId);



    }
}
