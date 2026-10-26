package com.dimension.controller.admin;

import com.dimension.controller.user.BaseUserServlet;

import cn.hutool.core.map.MapUtil;
import com.dimension.entity.User;
import com.dimension.utils.JsonResultOutputUtil;
import com.dimension.vo.ImageTagHistoryCategory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet(name = "GetUserTableWithUsername",value = "/GetUserTableWithUsername")
public class GetUserTableWithUsername extends BaseUserServlet{
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //1.获取数据
        String username = req.getParameter("username");
        // 2.建vo，如果有必要的话

        // 3.调用service层获取数据
        Long total;
        List<User> userList = userService.getUserTableWithUsername(username);
        if(null != userList && userList.size() > 0){
            total = 1L;
        }else {
            total = 0L;
        }
        // 4.组装数据
        Map<Object, Object> result = MapUtil.builder()
                .put("userList", userList)
                .put("total",total).build();
        // 5.输出数据
        JsonResultOutputUtil.outputJsonResult(resp, result);
    }
}
