package com.dimension.controller.rating;

import com.dimension.service.ImageRatingService;
import com.dimension.service.impl.ImageRatingServiceImpl;

import javax.servlet.http.HttpServlet;

public class BaseImageRatingServlet extends HttpServlet {
    protected ImageRatingService imageRatingService = new ImageRatingServiceImpl();
}
