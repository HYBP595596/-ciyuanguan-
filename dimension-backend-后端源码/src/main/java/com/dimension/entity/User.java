package com.dimension.entity;

import lombok.Data;

import java.util.Date;

/**
 * 用户表 dimension_user
 */
@Data
public class User {
    /**
     * 主键
     */
    private Long id;
    /**
     * 昵称
     */
    private String nickname;
    /**
     * 密码
     */
    private String password;
    /**
     * 简介
     */
    private String description;
    /**
     * 状态 (0-可用 1-禁用)
     */
    private Integer status;
    /**
     * 头像
     */
    private String avatar;
    /**
     * 账号
     */
    private String username;
    /**
     * 性别 (0-男 1-女 2-保密)
     */
    private Integer sex;
    /**
     * 出生日期
     */
    private Date birthday;
    /**
     * 主页背景图
     */
    private String coverImage;
}
