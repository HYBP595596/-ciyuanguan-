package com.dimension.entity;

import lombok.Data;

import java.util.Date;

/**
 * 全站公告 dimension_notice
 */
@Data
public class Notice {
    private Long id;
    private String title;
    private String content;
    private Long adminId;
    private Date createTime;

    // 非表字段：列表展示发布者昵称
    private String adminNickname;
}
