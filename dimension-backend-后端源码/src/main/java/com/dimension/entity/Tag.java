package com.dimension.entity;

import lombok.Data;

/**
 * 标签表 dimension_tag
 */
@Data
public class Tag {
    /**
     * 主键
     */
    private Integer id;
    /**
     * 标签名
     */
    private String name;
}
