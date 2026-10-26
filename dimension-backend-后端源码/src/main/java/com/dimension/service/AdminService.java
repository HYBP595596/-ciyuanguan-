package com.dimension.service;

import com.dimension.entity.Admin;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

public interface AdminService {

    Map<String, Object> adminLogin(String username, String password, HttpServletResponse resp);

    // ---------- 管理员账号管理 ----------
    List<Admin> getAdminList(Integer pageNum, Integer pageSize);

    long getAdminTotal();

    Admin getAdminById(Integer id);

    Admin getAdminByUsername(String username);

    /** 返回 null 表示成功，否则是给前端的错误提示 */
    String createAdmin(String username, String nickname, String password);

    String updateAdmin(Integer id, String username, String nickname);

    String resetPassword(Integer id, String newPassword);

    String changeOwnPassword(Integer id, String oldPassword, String newPassword);

    String deleteAdmin(Integer id, Integer operatorId);
}
