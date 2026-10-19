package com.dimension.controller.user;

import com.dimension.service.UserService;
import com.dimension.service.impl.UserServiceImpl;

import javax.servlet.http.HttpServlet;

public class BaseUserServlet extends HttpServlet {
    protected UserService userService = new UserServiceImpl();
}
