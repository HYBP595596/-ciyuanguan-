package com.dimension.entity;

import lombok.Data;

import java.util.Date;

/**
 * 图片评论表 dimension_comment
 */
@Data
public class Comment {
    /**
     * 主键
     */
    private Long id;
    /**
     * 用户id
     */
    private Long userId;
    /**
     * 图片id
     */
    private Long imageId;
    /**
     * 评论内容
     */
    private String content;
    /**
     * 评论时间
     */
    private Date createTime;
}
