package com.dimension.utils;

import javax.servlet.http.HttpServletRequest;

/**
 * 分页参数解析。
 * 缺参数、非数字、越界一律回落到默认值，避免 "Cannot parse null string" 之类的 500。
 */
public class PageUtil {

    private static final int DEFAULT_CURRENT_PAGE = 1;
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 200;

    /** 支持 currentPage / pageNum 两种叫法（前端历史遗留） */
    private static Integer getCurrentPage(HttpServletRequest req) {
        Integer page = parseInt(req.getParameter("currentPage"));
        if (page == null) {
            page = parseInt(req.getParameter("pageNum"));
        }
        if (page == null || page < 1) {
            page = DEFAULT_CURRENT_PAGE;
        }
        return page;
    }

    public static Integer getPageSize(HttpServletRequest req) {
        Integer size = parseInt(req.getParameter("pageSize"));
        if (size == null || size < 1) {
            size = DEFAULT_PAGE_SIZE;
        }
        if (size > MAX_PAGE_SIZE) {
            size = MAX_PAGE_SIZE;
        }
        return size;
    }

    public static Integer getPageNum(HttpServletRequest req) {
        Integer currentPage = getCurrentPage(req);
        return (currentPage - 1) * getPageSize(req);
    }

    private static Integer parseInt(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        try {
            return Integer.valueOf(trimmed);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
