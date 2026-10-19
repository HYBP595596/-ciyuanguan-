package com.dimension.controller.tagImageRelation;

import com.dimension.service.TagImageRelationService;
import com.dimension.service.impl.TagImageRelationServiceImpl;

import javax.servlet.http.HttpServlet;

public class BaseTagImageRelationServlet extends HttpServlet {
    protected TagImageRelationService tagImageRelationService = new TagImageRelationServiceImpl();
}
