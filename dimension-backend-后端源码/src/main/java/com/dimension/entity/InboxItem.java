package com.dimension.entity;

import lombok.Data;

import java.util.Date;

/** 消息中心的统一条目（私信 / 评论回复 / @提及 / 公告 / 作品更新） */
@Data
public class InboxItem {
    private String type;        // PRIVATE / REPLY / MENTION / ANNOUNCE / WORK_UPDATE
    private Long refId;         // 对应记录的 id
    private Long actorId;       // 触发人（公告为空）
    private String actorName;
    private String actorAvatar;
    private String title;       // 公告用
    private String content;
    private Long imageId;       // 评论/作品更新关联的作品
    private Date createTime;
}
