package com.dimension.controller.message;

import cn.hutool.core.map.MapUtil;
import com.dimension.entity.InboxItem;
import com.dimension.entity.User;
import com.dimension.mapper.FollowMapper;
import com.dimension.mapper.InboxMapper;
import com.dimension.mapper.MessageMapper;
import com.dimension.mapper.UserMapper;
import com.dimension.utils.JsonResultOutputUtil;
import com.dimension.utils.SessionUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 消息中心 /Inbox?action=xxx
 *   消息（顶部「消息」）= 私信 + 评论回复 + @提及
 *   通知（顶部「通知」）= 系统公告 + 关注画师的作品更新 + 活动推送（暂无数据源）
 *
 *   summary  各类未读数
 *   message  消息列表
 *   notice   通知列表
 *   read     把某一类标记为已读（写水位）
 */
@WebServlet(name = "Inbox", value = "/Inbox")
public class InboxServlet extends HttpServlet {

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
            InboxMapper mapper = SessionUtil.getMapper(InboxMapper.class);
            MessageMapper messageMapper = SessionUtil.getMapper(MessageMapper.class);
            UserMapper userMapper = SessionUtil.getMapper(UserMapper.class);

            Long userId = Long.valueOf(req.getParameter("userId"));
            User user = userMapper.getUserInfo(userId);
            String nickname = (user == null || user.getNickname() == null) ? "\u0000" : user.getNickname();

            switch (action) {
                case "summary": {
                    long privateUnread = messageMapper.getUnreadTotal(userId);
                    long replyUnread = mapper.countReplies(userId, lastId(mapper, userId, "REPLY"));
                    long mentionUnread = mapper.countMentions(userId, nickname, lastId(mapper, userId, "MENTION"));
                    long announceUnread = mapper.countAnnounces(lastId(mapper, userId, "ANNOUNCE"));
                    long workUnread = mapper.countWorkUpdates(userId, lastId(mapper, userId, "WORK_UPDATE"));

                    Map<Object, Object> data = MapUtil.builder()
                            .put("privateUnread", privateUnread)
                            .put("replyUnread", replyUnread)
                            .put("mentionUnread", mentionUnread)
                            .put("announceUnread", announceUnread)
                            .put("workUpdateUnread", workUnread)
                            .put("activityUnread", 0)
                            .put("messageUnread", privateUnread + replyUnread + mentionUnread)
                            .put("noticeUnread", announceUnread + workUnread)
                            .build();
                    JsonResultOutputUtil.outputJsonResult(resp, data);
                    return;
                }
                case "message": {
                    int pageNum = intParam(req, "pageNum", 0);
                    int pageSize = intParam(req, "pageSize", 20);
                    List<InboxItem> list = mapper.getMessageItems(userId, nickname, pageNum, pageSize);
                    JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                            .put("itemList", list).build());
                    return;
                }
                case "notice": {
                    int pageNum = intParam(req, "pageNum", 0);
                    int pageSize = intParam(req, "pageSize", 20);
                    List<InboxItem> list = mapper.getNoticeItems(userId, pageNum, pageSize);
                    JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                            .put("itemList", list).build());
                    return;
                }
                case "read": {
                    String type = req.getParameter("type");
                    if (type == null) type = "";
                    long maxId;
                    switch (type) {
                        case "REPLY":
                            maxId = mapper.getMaxReplyId(userId);
                            break;
                        case "MENTION":
                            maxId = mapper.getMaxMentionId(userId, nickname);
                            break;
                        case "ANNOUNCE":
                            maxId = mapper.getMaxAnnounceId();
                            break;
                        case "WORK_UPDATE":
                            maxId = mapper.getMaxWorkUpdateId(userId);
                            break;
                        case "ALL_NOTICE":
                            mapper.upsertLastId(userId, "ANNOUNCE", mapper.getMaxAnnounceId());
                            mapper.upsertLastId(userId, "WORK_UPDATE", mapper.getMaxWorkUpdateId(userId));
                            JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                                    .put("flag", 0).build());
                            return;
                        case "ALL_MESSAGE":
                            mapper.upsertLastId(userId, "REPLY", mapper.getMaxReplyId(userId));
                            mapper.upsertLastId(userId, "MENTION", mapper.getMaxMentionId(userId, nickname));
                            JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                                    .put("flag", 0).build());
                            return;
                        default:
                            JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                                    .put("flag", 1).put("message", "未知类型: " + type).build());
                            return;
                    }
                    mapper.upsertLastId(userId, type, maxId);
                    JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                            .put("flag", 0).put("type", type).put("lastId", maxId).build());
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

    private long lastId(InboxMapper mapper, Long userId, String type) {
        Long v = mapper.getLastId(userId, type);
        return v == null ? 0L : v;
    }

    private int intParam(HttpServletRequest req, String name, int def) {
        String v = req.getParameter(name);
        if (v == null || v.trim().isEmpty()) return def;
        try {
            return Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }
}
