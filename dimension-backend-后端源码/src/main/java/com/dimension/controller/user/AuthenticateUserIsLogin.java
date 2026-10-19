package com.dimension.controller.user;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.dimension.utils.JsonResultOutputUtil;
import com.dimension.utils.UserHolderUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

// 接口9.1--小徐
@WebServlet(name = "AuthenticateUserIsLogin", value = "/AuthenticateUserIsLogin")
public class AuthenticateUserIsLogin extends BaseUserServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 调用UserHolderUtil工具类get方法从ThreadLocal中取出User，
        // 如果不为 null 表示已登录 返回true ,否则返回false
        System.out.println(UserHolderUtil.getUser());
        boolean isLogin = !StrUtil.isEmpty(req.getHeader("token")) ? true : false;
        Map<Object, Object> result = MapUtil.builder()
                .put("isLogin", isLogin).build();
        JsonResultOutputUtil.outputJsonResult(resp, result);
    }
}
