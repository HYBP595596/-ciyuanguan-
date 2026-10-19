package com.dimension.controller.tag;

import com.dimension.service.TagService;
import com.dimension.service.impl.TagServiceImpl;

import javax.servlet.http.HttpServlet;

public class BaseTagServlet extends HttpServlet {
    protected TagService tagService = new TagServiceImpl();
}
