package com.dimension.controller.bookmark;

import cn.hutool.core.map.MapUtil;
import com.dimension.entity.ImageBookmark;
import com.dimension.service.ImageBookmarkService;
import com.dimension.service.impl.ImageBookmarkServiceImpl;
import com.dimension.utils.JsonResultOutputUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;


@WebServlet(name = "GetOneImageBookmark", value = "/GetOneImageBookmark")
public class GetimageBookmarkServlet extends BaseImageBookmarkServlet{
    //4.1  GetOneImageBookmark => 获取某条收藏记录

    protected ImageBookmarkService imageBookmarkService = new ImageBookmarkServiceImpl();
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        Long userId = Long.parseLong(req.getParameter("userId"));
        Long imageId = Long.parseLong(req.getParameter("imageId"));

        //调用service获取数据
        //查询一条收藏记录
        List<ImageBookmark> list = imageBookmarkService.getimageBookmark(userId,imageId);

        //组装数据
        Map<Object,Object> imageBookmark = MapUtil.builder().put("imageBookmark",list).build();

        //响应给前端
        JsonResultOutputUtil.outputJsonResult(resp,imageBookmark);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp);
    }
}
