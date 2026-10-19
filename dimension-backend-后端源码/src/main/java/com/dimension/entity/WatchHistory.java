package com.dimension.entity;

import lombok.Data;

import java.util.Date;

/**
 * 用户浏览历史表 dimension_watch_history
 */
@Data
public class WatchHistory {
    /**
     * 主键
     */
    private Long id;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 图片id
     */
    private Long imageId;
    /**
     * 用户id
     */
    private Long userId;
}
