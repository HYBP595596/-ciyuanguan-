package com.dimension.utils;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.dimension.entity.Admin;
import com.dimension.mapper.AdminMapper;

import javax.servlet.http.HttpServletRequest;

/**
 * 管理员身份解析与校验。
 *
 * 比单纯 JwtUtil.checkToken 更严格，多了两步：
 *   1. 校验 token 里的 tokenVersion 与数据库是否一致 —— 改密码后版本 +1，旧 token 立即失效
 *   2. 校验该管理员是否还存在 —— 账号被删后旧 token 也不能用
 */
public class AdminAuthUtil {

    /** 从请求里取 token：优先请求头，其次查询参数 */
    public static String extractToken(HttpServletRequest req) {
        String token = req.getHeader("AdminAuthentication");
        if (token == null || token.trim().isEmpty()) {
            token = req.getParameter("adminToken");
        }
        return (token == null) ? null : token.trim();
    }

    /** 解析出当前管理员；无效返回 null */
    public static Admin resolve(String token) {
        if (token == null || token.isEmpty() || !JwtUtil.checkToken(token)) {
            return null;
        }
        String json = JwtUtil.getObject(token);
        if (json == null) {
            return null;
        }
        try {
            JSONObject obj = JSONUtil.parseObj(json);
            Integer id = obj.getInt("id");
            Integer tokenVersion = obj.getInt("tokenVersion");
            if (id == null || tokenVersion == null) {
                return null; // 老版本 token（没有版本号）一律视为失效，重新登录即可
            }
            AdminMapper mapper = SessionUtil.getMapper(AdminMapper.class);
            Integer dbVersion = mapper.getTokenVersion(id);
            if (dbVersion == null) {
                return null; // 管理员已被删除
            }
            if (!dbVersion.equals(tokenVersion)) {
                return null; // 密码已修改，旧 token 作废
            }
            Admin admin = mapper.getAdminById(id);
            if (admin == null) {
                return null;
            }
            admin.setPassword(null);
            return admin;
        } catch (Exception e) {
            return null;
        }
    }
}
