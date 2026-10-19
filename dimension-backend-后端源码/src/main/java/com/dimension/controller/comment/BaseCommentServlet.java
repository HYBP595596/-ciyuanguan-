package com.dimension.controller.comment;

import com.dimension.service.CommentService;
import com.dimension.service.impl.CommentServiceImpl;

import javax.servlet.http.HttpServlet;

public class BaseCommentServlet extends HttpServlet {
    protected CommentService commentService = new CommentServiceImpl();
}
