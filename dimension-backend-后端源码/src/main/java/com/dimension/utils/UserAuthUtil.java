package com.dimension.utils;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.dimension.entity.User;
import com.dimension.mapper.UserMapper;

import javax.servlet.http.HttpServletRequest;

/**
 * 普通用户身份解析（对应请求头 Authentication）。
 * 校验：签名有效 + 载荷里有 id + 该用户仍然存在且未被禁用。
 */
public class UserAuthUtil {

    public static String extractToken(HttpServletRequest req) {
        String token = req.getHeader("Authentication");
        if (token == null || token.trim().isEmpty()) {
            token = req.getParameter("userToken");
        }
        return (token == null) ? null : token.trim();
    }

    public static User resolve(String token) {
        if (token == null || token.isEmpty() || !JwtUtil.checkToken(token)) {
            return null;
        }
        String json = JwtUtil.getObject(token);
        if (json == null) {
            return null;
        }
        try {
            JSONObject obj = JSONUtil.parseObj(json);
            Long id = obj.getLong("id");
            if (id == null) {
                return null;
            }
            UserMapper mapper = SessionUtil.getMapper(UserMapper.class);
            User user = mapper.getUserInfo(id);
            if (user == null) {
                return null;
            }
            // 被禁用（status=1）的用户视为无效会话
            if (user.getStatus() != null && user.getStatus() == 1) {
                return null;
            }
            user.setPassword(null);
            return user;
        } catch (Exception e) {
            return null;
        }
    }
}
