package com.dimension.controller.comment;

import cn.hutool.json.JSONUtil;
import com.dimension.controller.image.BaseImageServlet;
import com.dimension.entity.Comment;
import com.dimension.service.CommentService;
import com.dimension.utils.JsonParameterReaderUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;

@WebServlet(name = "PostCommentImage" ,value = "/CommentImage")
public class PostCommentImage extends BaseCommentServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Map map= JsonParameterReaderUtil.readJson(req);
        String content=map.get("content").toString();
        Long imageId=Long.valueOf(map.get("imageId").toString());
        Long userId=Long.valueOf(map.get("userId").toString());
        //String content=req.getParameter("content");
        //long imageId = Long.parseLong(req.getParameter("imageId"));
        //long UserId = Long.parseLong(req.getParameter("UserId"));

        commentService.addCommentImage(content,imageId,userId);


    }

}
