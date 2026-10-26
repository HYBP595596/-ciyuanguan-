package com.dimension.controller.bookmark;

import com.dimension.service.ImageBookmarkService;
import com.dimension.service.impl.ImageBookmarkServiceImpl;

import javax.servlet.http.HttpServlet;

public class BaseImageBookmarkServlet extends HttpServlet {
    protected ImageBookmarkService imageBookmarkService = new ImageBookmarkServiceImpl();
}
