package com.dimension.service.impl;

import com.dimension.entity.Message;
import com.dimension.mapper.MessageMapper;
import com.dimension.service.MessageService;
import com.dimension.utils.SessionUtil;

import java.util.List;

public class MessageServiceImpl implements MessageService {

    private final MessageMapper mapper = SessionUtil.getMapper(MessageMapper.class);

    @Override
    public boolean send(Long senderId, Long receiverId, String content) {
        if (senderId == null || receiverId == null) {
            return false;
        }
        if (senderId.equals(receiverId)) {
            return false; // 不能给自己发
        }
        String text = content == null ? "" : content.trim();
        if (text.isEmpty()) {
            return false;
        }
        if (text.length() > 1000) {
            text = text.substring(0, 1000);
        }
        Message msg = new Message();
        msg.setSenderId(senderId);
        msg.setReceiverId(receiverId);
        msg.setContent(text);
        mapper.insertMessage(msg);
        return true;
    }

    @Override
    public List<Message> getConversation(Long userId, Long peerId, Integer pageNum, Integer pageSize) {
        // 打开对话即把对方发给我的未读清掉
        mapper.markRead(peerId, userId);
        int offset = (pageNum == null) ? 0 : pageNum;
        int limit = (pageSize == null || pageSize <= 0) ? 100 : pageSize;
        return mapper.getConversation(userId, peerId, offset, limit);
    }

    @Override
    public List<Message> getSessions(Long userId) {
        return mapper.getSessions(userId);
    }

    @Override
    public long getUnreadTotal(Long userId) {
        return mapper.getUnreadTotal(userId);
    }
}
