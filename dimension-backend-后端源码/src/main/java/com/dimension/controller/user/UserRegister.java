package com.dimension.controller.user;

import cn.hutool.core.map.MapUtil;
import com.dimension.utils.JsonResultOutputUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

// 接口9.6--小徐
@WebServlet(name = "UserRegister", value = "/UserRegister")
public class UserRegister extends BaseUserServlet{
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 请求参数 nickname、username、password
        String nickname = req.getParameter("nickname");
        String username = req.getParameter("username");
        String password = req.getParameter("password");

        // 调用service处理数据 resRegisterUser
        int flag =  userService.registerUser(nickname, username, password);
        // 组装map
        Map<Object, Object> registerResult = MapUtil.builder()
                .put("flag", flag).build();
        // 响应
        JsonResultOutputUtil.outputJsonResult(resp, registerResult);
    }
}
