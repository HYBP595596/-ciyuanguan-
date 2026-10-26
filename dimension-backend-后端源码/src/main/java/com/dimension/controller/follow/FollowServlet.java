package com.dimension.controller.follow;

import cn.hutool.core.map.MapUtil;
import com.dimension.entity.User;
import com.dimension.mapper.FollowMapper;
import com.dimension.utils.JsonResultOutputUtil;
import com.dimension.utils.SessionUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 关注/粉丝的聚合查询 /Follow?action=counts|following|fans
 * （关注、取关仍走已有的 /FollowUser、/UnfollowUser）
 */
@WebServlet(name = "FollowQuery", value = "/Follow")
public class FollowServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handle(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handle(req, resp);
    }

    private void handle(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String action = req.getParameter("action");
        if (action == null) action = "";
        try {
            FollowMapper mapper = SessionUtil.getMapper(FollowMapper.class);
            Long userId = Long.valueOf(req.getParameter("userId"));

            switch (action) {
                case "counts": {
                    JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                            .put("followingCount", mapper.getFollowingCount(userId))
                            .put("fansCount", mapper.getFansCount(userId))
                            .build());
                    return;
                }
                case "following": {
                    List<User> list = mapper.getFollowingUsers(userId);
                    clearPassword(list);
                    JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                            .put("userList", list).build());
                    return;
                }
                case "fans": {
                    List<User> list = mapper.getFansUsers(userId);
                    clearPassword(list);
                    JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                            .put("userList", list).build());
                    return;
                }
                default:
                    JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                            .put("flag", 1).put("message", "未知操作: " + action).build());
            }
        } catch (Exception e) {
            JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                    .put("flag", 1).put("message", "参数错误: " + e.getMessage()).build());
        }
    }

    private void clearPassword(List<User> list) {
        for (User u : list) {
            u.setPassword(null);
        }
    }
}
