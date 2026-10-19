package com.dimension.entity;

import lombok.Data;

/**
 * 图片分类表 dimension_image_category
 */
@Data
public class ImageCategory {
    /**
     * 主键
     */
    private Long id;
    /**
     * 分类名称
     */
    private String name;
    /**
     * 分类下的图片数
     */
    private Integer count;
    /**
     * 排序 - 0~100，低到高
     */
    private Integer sort;
}
