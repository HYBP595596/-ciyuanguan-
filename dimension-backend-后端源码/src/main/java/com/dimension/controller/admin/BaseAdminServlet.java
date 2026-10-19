package com.dimension.controller.admin;

import com.dimension.service.AdminService;
import com.dimension.service.impl.AdminServiceImpl;

import javax.servlet.http.HttpServlet;

public class BaseAdminServlet extends HttpServlet {
    protected AdminService adminService = new AdminServiceImpl();
}
