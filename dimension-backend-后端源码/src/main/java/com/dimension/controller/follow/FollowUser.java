package com.dimension.controller.follow;

import com.dimension.mapper.FollowMapper;
import com.dimension.utils.SessionUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "FollowUser", value = "/FollowUser")
public class FollowUser extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long userId = Long.valueOf(req.getParameter("userId"));
        Long targetUserId = Long.valueOf(req.getParameter("targetUserId"));
        
        FollowMapper mapper = SessionUtil.getMapper(FollowMapper.class);
        // 如果没关注过，才插入
        if (mapper.checkIsFollowing(userId, targetUserId) == 0) {
            mapper.insertFollow(userId, targetUserId);
        }
    }
}