package com.dimension.controller.message;

import cn.hutool.core.map.MapUtil;
import com.dimension.entity.Message;
import com.dimension.service.MessageService;
import com.dimension.service.impl.MessageServiceImpl;
import com.dimension.utils.JsonResultOutputUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 私信：发送 / 聊天记录 / 会话列表 / 未读数
 * 统一入口 /Message?action=xxx
 */
@WebServlet(name = "Message", value = "/Message")
public class MessageServlet extends HttpServlet {

    private final MessageService messageService = new MessageServiceImpl();

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
        if (action == null) {
            action = "";
        }
        try {
            switch (action) {
                case "send": {
                    Long senderId = Long.valueOf(req.getParameter("senderId"));
                    Long receiverId = Long.valueOf(req.getParameter("receiverId"));
                    String content = req.getParameter("content");
                    boolean ok = messageService.send(senderId, receiverId, content);
                    JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                            .put("flag", ok ? 0 : 1)
                            .put("message", ok ? "已发送" : "内容为空或不能发给自己")
                            .build());
                    return;
                }
                case "conversation": {
                    Long userId = Long.valueOf(req.getParameter("userId"));
                    Long peerId = Long.valueOf(req.getParameter("peerId"));
                    String pn = req.getParameter("pageNum");
                    String ps = req.getParameter("pageSize");
                    List<Message> list = messageService.getConversation(
                            userId, peerId,
                            pn == null ? 0 : Integer.valueOf(pn),
                            ps == null ? 100 : Integer.valueOf(ps));
                    JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                            .put("messageList", list).build());
                    return;
                }
                case "sessions": {
                    Long userId = Long.valueOf(req.getParameter("userId"));
                    List<Message> list = messageService.getSessions(userId);
                    JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                            .put("sessionList", list).build());
                    return;
                }
                case "unread": {
                    Long userId = Long.valueOf(req.getParameter("userId"));
                    JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                            .put("unread", messageService.getUnreadTotal(userId)).build());
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
}
