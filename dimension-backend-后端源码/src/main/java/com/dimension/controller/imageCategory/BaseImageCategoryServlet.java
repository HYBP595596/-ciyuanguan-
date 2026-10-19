package com.dimension.controller.imageCategory;

import com.dimension.service.ImageCategoryService;
import com.dimension.service.impl.ImageCategoryServiceImpl;

import javax.servlet.http.HttpServlet;

public class BaseImageCategoryServlet extends HttpServlet {
    protected ImageCategoryService imageCategoryService = new ImageCategoryServiceImpl();
}
