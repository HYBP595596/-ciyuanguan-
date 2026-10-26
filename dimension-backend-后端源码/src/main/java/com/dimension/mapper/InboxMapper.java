package com.dimension.mapper;

import com.dimension.entity.InboxItem;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface InboxMapper {

    // ---------- 已读水位 ----------
    Long getLastId(@Param("userId") Long userId, @Param("type") String type);

    int upsertLastId(@Param("userId") Long userId, @Param("type") String type, @Param("lastId") Long lastId);

    // ---------- 未读数 ----------
    long countReplies(@Param("userId") Long userId, @Param("sinceId") Long sinceId);

    long countMentions(@Param("userId") Long userId, @Param("nickname") String nickname,
                       @Param("sinceId") Long sinceId);

    long countAnnounces(@Param("sinceId") Long sinceId);

    long countWorkUpdates(@Param("userId") Long userId, @Param("sinceId") Long sinceId);

    // ---------- 列表 ----------
    List<InboxItem> getMessageItems(@Param("userId") Long userId, @Param("nickname") String nickname,
                                    @Param("pageNum") Integer pageNum, @Param("pageSize") Integer pageSize);

    List<InboxItem> getNoticeItems(@Param("userId") Long userId,
                                   @Param("pageNum") Integer pageNum, @Param("pageSize") Integer pageSize);

    // ---------- 各类当前最大 id（打已读水位用） ----------
    Long getMaxReplyId(@Param("userId") Long userId);

    Long getMaxMentionId(@Param("userId") Long userId, @Param("nickname") String nickname);

    Long getMaxAnnounceId();

    Long getMaxWorkUpdateId(@Param("userId") Long userId);
}
