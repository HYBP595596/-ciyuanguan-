package com.dimension.filter;

import cn.hutool.core.map.MapUtil;
import com.dimension.entity.Admin;
import com.dimension.utils.AdminAuthUtil;
import com.dimension.utils.JsonResultOutputUtil;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 管理员鉴权过滤器。
 *
 * 采用「黑名单」策略：只拦截明确属于后台管理的接口，其余（登录、前台浏览、用户操作）全部放行，
 * 避免把整站请求都要求管理员身份而误伤。
 *
 * 注意两点：
 *   1. OPTIONS 预检请求必须直接放行，否则浏览器跨域预检会被 401 挡掉，所有后台请求都发不出去
 *   2. 判断依据用 getServletPath()（形如 /AuditImage），不依赖带 contextPath 的 URI
 */
public class AdminAuthenticationFilter implements Filter {

    /** 必须管理员身份才能访问的接口 */
    private static final Set<String> PROTECTED = new HashSet<>(Arrays.asList(
            // 后台列表类
            "/GetImageTableWithNoCondition",
            "/GetImageTableWithCondition",
            "/GetUserTableWithNoCondition",
            "/GetUserTableWithNickname",
            "/GetUserTableWithUsername",
            "/GetCommentTableWithNoCondition",
            "/GetCommentTableWithCondition",
            // 审核
            "/GetPendingImage",
            "/AuditImage",
            "/GetPendingArtist",
            "/AuditArtist",
            // 用户/评论管理
            "/ForbidUser",
            "/ResumeUser",
            "/RemoveCommentById",
            // 分类管理
            "/CategoryAdmin",
            // 管理员账号管理
            "/AdminAccount",
            // 订单管理（列表/强制退款/强制关单）
            "/AdminOrder"
    ));

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse resp = (HttpServletResponse) servletResponse;

        // 跨域预检不带自定义头，必须放行
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            chain.doFilter(servletRequest, servletResponse);
            return;
        }

        String path = req.getServletPath();
        boolean needAuth = path != null && PROTECTED.contains(path);

        // 公告的写操作也要管理员，读操作（list/latest）保持公开
        if (!needAuth && "/Notice".equals(path)) {
            String action = req.getParameter("action");
            needAuth = "add".equals(action) || "delete".equals(action);
        }

        if (!needAuth) {
            chain.doFilter(servletRequest, servletResponse);
            return;
        }

        Admin admin = AdminAuthUtil.resolve(AdminAuthUtil.extractToken(req));
        if (admin == null) {
            resp.setStatus(401);
            JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                    .put("flag", 1)
                    .put("needLogin", true)
                    .put("message", "管理员未登录或登录已过期")
                    .build());
            return;
        }

        // 供 Servlet 直接取用，避免重复解析
        req.setAttribute("currentAdmin", admin);
        chain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {
    }
}
