package com.dimension.controller.category;

import com.dimension.service.ImageCategoryService;
import com.dimension.service.impl.ImageCategoryServiceImpl;

import javax.servlet.http.HttpServlet;

public class BaseImageCategoryServlet extends HttpServlet {
    protected ImageCategoryService imageCategoryService = new ImageCategoryServiceImpl();
}
