package com.dimension.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 图片表 dimension_image
 */
@Data
public class Image {
    /**
     * 主键
     */
    private Long id;
    /**
     * 分类id
     */
    private Integer categoryId;
    /**
     * 图片地址
     */
    private String url;
    /**
     * 分数 - 0.0~10.0
     */
    private float score;
    /**
     * 收藏数
     */
    private Integer collects;
    /**
     * 创建时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    /**
     * 评论数
     */
    private Integer comments;
    /**
     * 投票数
     */
    private Integer votes;
    /**
     * 浏览数
     */
    private Integer views;
}
