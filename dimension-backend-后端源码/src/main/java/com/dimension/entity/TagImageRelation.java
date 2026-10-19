package com.dimension.entity;

import lombok.Data;

/**
 * 图片标签关联表 dimension_tag_image_relation
 */
@Data
public class TagImageRelation {
    /**
     * 主键
     */
    private Long id;
    /**
     * 标签id
     */
    private Integer tagId;
    /**
     * 图片id
     */
    private Long imageId;
}
