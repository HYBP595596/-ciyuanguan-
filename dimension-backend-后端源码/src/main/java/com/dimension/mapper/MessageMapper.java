package com.dimension.mapper;

import com.dimension.entity.Message;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MessageMapper {

    int insertMessage(Message message);

    /** 两人之间的聊天记录（按时间正序） */
    List<Message> getConversation(@Param("userId") Long userId,
                                  @Param("peerId") Long peerId,
                                  @Param("pageNum") Integer pageNum,
                                  @Param("pageSize") Integer pageSize);

    /** 会话列表：每个对话对象的最后一条 + 未读数 */
    List<Message> getSessions(@Param("userId") Long userId);

    /** 把某人和我之间的未读消息标记为已读 */
    int markRead(@Param("fromId") Long fromId, @Param("toId") Long toId);

    long getUnreadTotal(@Param("userId") Long userId);
}
