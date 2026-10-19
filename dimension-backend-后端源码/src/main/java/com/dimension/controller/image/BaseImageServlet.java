package com.dimension.controller.image;

import com.dimension.service.ImageService;
import com.dimension.service.impl.ImageServiceImpl;

import javax.servlet.http.HttpServlet;

public class BaseImageServlet extends HttpServlet {
    protected ImageService imageService = new ImageServiceImpl();
}
