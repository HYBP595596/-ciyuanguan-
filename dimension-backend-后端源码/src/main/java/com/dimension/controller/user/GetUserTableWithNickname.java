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

// 接口9.7--小徐
@WebServlet(name = "GetUserTableWithNickname", value = "/GetUserTableWithNickname")
public class GetUserTableWithNickname extends BaseUserServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 获取请求数据 nickname、currentPage、pageSize
        Integer pageNum = PageUtil.getPageNum(req);
        Integer pageSize = PageUtil.getPageSize(req);
        String nickname = req.getParameter("nickname");

        // 根据nickname昵称模糊匹配搜索用户并分页，再查出模糊匹配后的total数
        List<User> userList = userService.getUserTableWithNickname(pageNum, pageSize, nickname);
        long total = userService.getTotalWithNickname(nickname);

        // 响应数据 userList、total
        JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                .put("userList", userList)
                .put("total", total).build());
    }
}
