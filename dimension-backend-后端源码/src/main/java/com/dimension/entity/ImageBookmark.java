package com.dimension.entity;

import lombok.Data;

import java.util.Date;

/**
 * 图片收藏夹表 dimension_image_bookmark
 */
@Data
public class ImageBookmark {
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
     * 收藏时间
     */
    private Date createTime;
}
