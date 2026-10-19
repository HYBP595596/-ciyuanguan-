package com.dimension.controller.user;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.json.JSONUtil;
import com.dimension.utils.JsonParameterReaderUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

// 接口9.4--小徐
@WebServlet(name = "UpdateUserInfo", value = "/UpdateUserInfo")
public class UpdateUserInfo extends BaseUserServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 请求参数 【JSON参数】nickname、description、sex、birthday、userId
        Map map = JsonParameterReaderUtil.readJson(req);
        String nickname = (String) map.get("nickname");
        String description = (String) map.get("description");
        Integer sex = Integer.parseInt(map.get("sex").toString()); // 0/1/2
        String birthday1 = (String) map.get("birthday"); // 2023-05-17 10:38:16
        Date birthday = DateUtil.parse(birthday1);
        Long userId = null; // 11
        try {
            userId = Long.parseLong(map.get("userId").toString());
        } catch (NumberFormatException e) {
            System.err.println("字符串 " + userId + " 无法被解析为long ");
        }

        // 根据用户id修改用户的nickname、description、sex、birthday信息
        // 调用service修改数据
        int resUpdateUserInfo = userService.updateUserInfo(userId, nickname, description, sex, birthday);
        System.out.println("resUpdateUserInfo = " + resUpdateUserInfo);
    }
}
