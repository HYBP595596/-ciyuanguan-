package com.dimension.service;

import com.dimension.entity.Message;

import java.util.List;

public interface MessageService {

    boolean send(Long senderId, Long receiverId, String content);

    /** 会同时把对方发给我的消息标记为已读 */
    List<Message> getConversation(Long userId, Long peerId, Integer pageNum, Integer pageSize);

    List<Message> getSessions(Long userId);

    long getUnreadTotal(Long userId);
}
