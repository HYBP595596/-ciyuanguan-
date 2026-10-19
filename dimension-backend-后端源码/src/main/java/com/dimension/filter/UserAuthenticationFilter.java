package com.dimension.filter;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.dimension.entity.User;
import com.dimension.utils.JwtUtil;
import com.dimension.utils.UserHolderUtil;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class UserAuthenticationFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse resp = (HttpServletResponse) servletResponse;

        String token = req.getHeader("Authentication");
//        if (StrUtil.isEmpty(token) || JwtUtil.isNull(token)) {
//            filterChain.doFilter(servletRequest, servletResponse);
//            return;
//        }

//        User user = JSONUtil.toBean(JwtUtil.getObject(token), User.class);

        // 这里还要进行token和cookie的刷新
//        String newToken = JwtUtil.createToken(user);
//        Cookie cookie1 = new Cookie("token", URLEncoder.encode(newToken, "utf-8"));
//        Cookie cookie2 = new Cookie("user", URLEncoder.encode(JSONUtil.toJsonStr(user), "utf-8"));
//        cookie1.setMaxAge(60 * 60 * 24 * 30);
//        cookie1.setPath("/");
//        cookie2.setMaxAge(60 * 60 * 24 * 30);
//        cookie2.setPath("/");
//        resp.addCookie(cookie1);
//        resp.addCookie(cookie2);

//        UserHolderUtil.saveUser(user);
        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {
        UserHolderUtil.removeUser();
    }
}
