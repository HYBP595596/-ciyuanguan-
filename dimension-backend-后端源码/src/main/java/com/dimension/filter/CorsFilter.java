package com.dimension.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CorsFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    /*处理前后端分离跨域问题*/
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        response.setHeader("Access-Control-Allow-Origin", "*"); // 允许任何域名访问
        response.setHeader("Access-Control-Allow-Methods", "*"); // 允许的请求方法
        response.setHeader("Access-Control-Max-Age", "3600"); // 缓存时间（秒）
        response.setHeader("Access-Control-Allow-Headers", "*"); // 允许的请求头字段
        chain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {
    }
}


