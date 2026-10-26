package com.dimension.controller.admin;

import com.dimension.controller.comment.BaseCommentServlet;

import com.dimension.utils.JsonParameterReaderUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

    @WebServlet(name = "PostComment",value = "/RemoveCommentById")
public class PostComment extends  BaseCommentServlet{

     @Override
    protected void doPost(HttpServletRequest req,HttpServletResponse response) throws IOException {
//         Map map= JsonParameterReaderUtil.readJson(req);
//         Long commentid=Long.valueOf(map.get("commentId").toString());

         long commentId = Long.parseLong(req.getParameter("commentId"));

         commentService.removecomment(commentId);
     }
}
