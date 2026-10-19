package com.dimension.filter;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.dimension.utils.JsonResultOutputUtil;
import com.dimension.utils.JwtUtil;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.List;

public class AdminAuthenticationFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
//        HttpServletRequest req = (HttpServletRequest) servletRequest;
//        HttpServletResponse resp = (HttpServletResponse) servletResponse;
//
//        // 接口黑名单 - 无条件查询图片、无条件查询评论、无条件查询用户
//        List<String> blacklist = Arrays.asList("/GetUserTableWithNoCondition",
//                "/GetImageTableWithNoCondition", "/GetCommentTableWithNoCondition");
//
//        if ("/dimension/".equals(req.getRequestURI())) {
//            filterChain.doFilter(servletRequest, servletResponse);
//            return;
//        }
//
//        // 不在接口黑名单中的请求都放行
//        String currentPath = "/" + req.getRequestURI().split("/")[2];
//        if (!blacklist.contains(currentPath)) {
//            filterChain.doFilter(servletRequest, servletResponse);
//            return;
//        }
//
//        // 在的话进行拦截
//        String adminToken = req.getHeader("AdminAuthentication");
//
//        if (StrUtil.isEmpty(adminToken) || !JwtUtil.checkToken(adminToken) || JwtUtil.isNull(adminToken)) {
//            resp.setStatus(600);
//            JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
//                    .put("message", "管理员请先登录")
//                    .put("status", 600).build());
//            return; // return拦截
//        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {

    }
}
