package com.dimension.controller.follow;

import cn.hutool.core.map.MapUtil;
import com.dimension.mapper.FollowMapper;
import com.dimension.utils.JsonResultOutputUtil;
import com.dimension.utils.SessionUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@WebServlet(name = "CheckFollowStatus", value = "/CheckFollowStatus")
public class CheckFollowStatus extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long userId = Long.valueOf(req.getParameter("userId"));
        Long targetUserId = Long.valueOf(req.getParameter("targetUserId"));
        
        FollowMapper mapper = SessionUtil.getMapper(FollowMapper.class);
        int isFollowing = mapper.checkIsFollowing(userId, targetUserId);
        int fansCount = mapper.getFansCount(targetUserId);

        Map<Object, Object> result = MapUtil.builder()
                .put("isFollowing", isFollowing > 0)
                .put("fansCount", fansCount)
                .build();
        JsonResultOutputUtil.outputJsonResult(resp, result);
    }
}