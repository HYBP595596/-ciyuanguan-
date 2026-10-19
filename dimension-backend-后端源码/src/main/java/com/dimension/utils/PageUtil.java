package com.dimension.utils;

import javax.servlet.http.HttpServletRequest;

public class PageUtil {
    private static Integer getCurrentPage(HttpServletRequest req) {
        return Integer.parseInt(req.getParameter("currentPage"));
    }

    public static Integer getPageSize(HttpServletRequest req) {
        return Integer.parseInt(req.getParameter("pageSize"));
    }

    public static Integer getPageNum(HttpServletRequest req) {
        Integer currentPage = getCurrentPage(req);
        return (currentPage - 1) * getPageSize(req);
    }
}
