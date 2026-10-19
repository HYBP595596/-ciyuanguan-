package com.dimension.service.impl;

import cn.hutool.json.JSONUtil;
import com.dimension.entity.Admin;
import com.dimension.mapper.AdminMapper;
import com.dimension.service.AdminService;
import com.dimension.utils.JwtUtil;
import com.dimension.utils.SessionUtil;
import io.jsonwebtoken.Jwt;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class AdminServiceImpl implements AdminService {
    private AdminMapper adminMapper = SessionUtil.getMapper(AdminMapper.class);

    @Override
    public Map<String, Object> adminLogin(String username, String password, HttpServletResponse resp) {
        Map<String, Object> result = new HashMap<>();

        Admin admin = adminMapper.adminLogin(username, password);
        if (admin == null) {
            result.put("flag", 1);
            return result;
        }

        String adminToken = JwtUtil.createToken(admin);

//        try {
//            String token = JwtUtil.createToken(admin);
//            Cookie cookie1 = new Cookie("admin", URLEncoder.encode(JSONUtil.toJsonStr(admin), "utf-8"));
//            Cookie cookie2 = new Cookie("adminToken", URLEncoder.encode(token, "utf-8"));
//            cookie1.setPath("/");
//            cookie2.setPath("/");
//            resp.addCookie(cookie1);
//            resp.addCookie(cookie2);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        result.put("admin", admin);
        result.put("adminToken", adminToken);
        result.put("flag", 0);

        return result;
    }
}
