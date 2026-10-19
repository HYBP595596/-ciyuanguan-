package com.dimension.controller.imageBookmark;

import com.dimension.service.ImageBookmarkService;
import com.dimension.service.impl.ImageBookmarkServiceImpl;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@WebServlet(name = "RemoveImageBookmark", value = "/RemoveImageBookmark")
public class RemoveImageBookmarkServlet extends BaseImageBookmarkServlet{
    //4.3  RemoveImageBookmark => 取消收藏

    protected ImageBookmarkService imageBookmarkService = new ImageBookmarkServiceImpl();
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long userId = Long.parseLong(req.getParameter("userId"));
        Long imageId = Long.parseLong(req.getParameter("imageId"));

        //根据图片id和用户id删除收藏记录
        imageBookmarkService.deleteImageBookmark(userId,imageId);

    }
}
