package com.dimension.service;

import com.dimension.entity.Admin;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

public interface AdminService {
    Map<String, Object> adminLogin(String username, String password, HttpServletResponse resp);
}
