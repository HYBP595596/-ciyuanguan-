package com.dimension.entity;

import lombok.Data;

/**
 * 图片评分表 dimension_image_rating
 */
@Data
public class ImageRating {
    /**
     * 主键
     */
    private Long id;
    /**
     * 评分用户id
     */
    private Long userId;
    /**
     * 图片id
     */
    private Long imageId;
    /**
     * 点赞或拉踩 (0-点赞 1-拉踩)
     */
    private Integer like;
}
