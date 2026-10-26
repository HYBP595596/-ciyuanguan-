package com.dimension.service.impl;

import com.dimension.entity.Admin;
import com.dimension.mapper.AdminMapper;
import com.dimension.service.AdminService;
import com.dimension.utils.JwtUtil;
import com.dimension.utils.PasswordUtil;
import com.dimension.utils.SessionUtil;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminServiceImpl implements AdminService {

    private AdminMapper adminMapper = SessionUtil.getMapper(AdminMapper.class);

    @Override
    public Map<String, Object> adminLogin(String username, String password, HttpServletResponse resp) {
        Map<String, Object> result = new HashMap<>();
        if (username == null || password == null) {
            result.put("flag", 1);
            return result;
        }
        Admin admin = adminMapper.getAdminByUsername(username);
        if (admin == null || !PasswordUtil.matches(password, admin.getPassword())) {
            result.put("flag", 1);
            return result;
        }
        // 老账号存的是明文密码，登录成功后就地升级为加盐哈希
        if (PasswordUtil.isLegacy(admin.getPassword())) {
            adminMapper.updateAdminPassword(admin.getId(), PasswordUtil.hash(password));
        }
        // 绝不把密码带出服务端（响应体和 token 里都不带）
        admin.setPassword(null);

        String adminToken = JwtUtil.createToken(admin);
        result.put("admin", admin);
        result.put("adminToken", adminToken);
        result.put("flag", 0);
        return result;
    }

    // ==================== 管理员账号管理 ====================

    @Override
    public List<Admin> getAdminList(Integer pageNum, Integer pageSize) {
        List<Admin> list = adminMapper.getAdminList(pageNum, pageSize);
        for (Admin a : list) {
            a.setPassword(null);
        }
        return list;
    }

    @Override
    public long getAdminTotal() {
        return adminMapper.getAdminTotal();
    }

    @Override
    public Admin getAdminById(Integer id) {
        Admin admin = adminMapper.getAdminById(id);
        if (admin != null) admin.setPassword(null);
        return admin;
    }

    @Override
    public Admin getAdminByUsername(String username) {
        return adminMapper.getAdminByUsername(username);
    }

    @Override
    public String createAdmin(String username, String nickname, String password) {
        String err = validateUsername(username);
        if (err != null) return err;
        if (password == null || password.length() < 6) {
            return "密码至少 6 位";
        }
        if (adminMapper.getAdminByUsername(username.trim()) != null) {
            return "账号已存在";
        }
        Admin admin = new Admin();
        admin.setUsername(username.trim());
        admin.setNickname((nickname == null || nickname.trim().isEmpty()) ? username.trim() : nickname.trim());
        admin.setPassword(PasswordUtil.hash(password));
        adminMapper.insertAdmin(admin);
        return null;
    }

    @Override
    public String updateAdmin(Integer id, String username, String nickname) {
        Admin existing = adminMapper.getAdminById(id);
        if (existing == null) return "管理员不存在";
        String err = validateUsername(username);
        if (err != null) return err;

        Admin sameName = adminMapper.getAdminByUsername(username.trim());
        if (sameName != null && !sameName.getId().equals(id)) {
            return "账号已被占用";
        }
        String nick = (nickname == null || nickname.trim().isEmpty())
                ? existing.getNickname() : nickname.trim();
        adminMapper.updateAdminInfo(id, username.trim(), nick);
        return null;
    }

    @Override
    public String resetPassword(Integer id, String newPassword) {
        if (adminMapper.getAdminById(id) == null) return "管理员不存在";
        if (newPassword == null || newPassword.length() < 6) {
            return "新密码至少 6 位";
        }
        adminMapper.updateAdminPassword(id, PasswordUtil.hash(newPassword));
        // 版本号 +1：该管理员此前签发的 token 全部失效，需要重新登录
        adminMapper.bumpTokenVersion(id);
        return null;
    }

    @Override
    public String changeOwnPassword(Integer id, String oldPassword, String newPassword) {
        Admin admin = adminMapper.getAdminById(id);
        if (admin == null) return "管理员不存在";
        if (!PasswordUtil.matches(oldPassword, admin.getPassword())) {
            return "原密码不正确";
        }
        if (newPassword == null || newPassword.length() < 6) {
            return "新密码至少 6 位";
        }
        if (newPassword.equals(oldPassword)) {
            return "新密码不能与原密码相同";
        }
        adminMapper.updateAdminPassword(id, PasswordUtil.hash(newPassword));
        // 版本号 +1：旧 token 作废（Servlet 会给当前操作者重新发一个）
        adminMapper.bumpTokenVersion(id);
        return null;
    }

    @Override
    public String deleteAdmin(Integer id, Integer operatorId) {
        if (id == null) return "参数错误";
        if (id.equals(operatorId)) {
            return "不能删除当前登录的账号";
        }
        if (adminMapper.getAdminById(id) == null) return "管理员不存在";
        if (adminMapper.countAdmins() <= 1) {
            return "至少要保留一个管理员";
        }
        adminMapper.deleteAdmin(id);
        return null;
    }

    private String validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) return "请填写账号";
        String u = username.trim();
        if (u.length() < 3 || u.length() > 20) return "账号长度需在 3~20 位之间";
        if (!u.matches("[A-Za-z0-9_]+")) return "账号只能包含字母、数字、下划线";
        return null;
    }
}
