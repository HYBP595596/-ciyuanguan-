package com.dimension.controller.user;

import cn.hutool.core.map.MapUtil;
import com.dimension.entity.User;
import com.dimension.utils.JsonResultOutputUtil;
import com.dimension.utils.PageUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet(name = "GetUserTableWithNoCondition",value = "/GetUserTableWithNoCondition")
public class GetUserTableWithNoCondition extends BaseUserServlet{
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer pageNum = PageUtil.getPageNum(req);
        Integer pageSize = PageUtil.getPageSize(req);

        List<User> userList = userService.getUserTableWithNoCondition(pageNum,pageSize);
        Long total = userService.getTotalWithNoCondition();

        Map<Object, Object> result = MapUtil.builder()
                .put("userList", userList)
                .put("total",total).build();

        JsonResultOutputUtil.outputJsonResult(resp, result);
    }
}
