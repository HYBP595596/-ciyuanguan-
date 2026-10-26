package com.dimension.controller.user;

import cn.hutool.core.map.MapUtil;
import com.dimension.entity.User;
import com.dimension.utils.JsonResultOutputUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

// 接口9.2--小徐
@WebServlet(name = "GetUserInfo", value = "/GetUserInfo")
public class GetUserInfo extends BaseUserServlet{
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 1\获取请求参数 userId
        String userId = req.getParameter("userId");
        // service获取数据
        User user = null;
        try {
            user = userService.getUserInfo(Long.parseLong(userId));
        } catch (NumberFormatException e) {
            System.err.println("字符串 " + userId + " 无法被解析为long ");
        }
        // 封装Map（绝不把密码返回给前端）
        if (user != null) {
            user.setPassword(null);
        }
        Map<Object, Object> userInfoResult = MapUtil.builder()
                .put("user", user).build();
        // 响应数据user
        JsonResultOutputUtil.outputJsonResult(resp, userInfoResult);
    }
}
