package com.dimension.entity;

import lombok.Data;

/**
 * 管理员表 dimension_admin
 */
@Data
public class Admin {
    /**
     * 主键
     */
    private Integer id;
    /**
     * 头像
     */
    private String avatar;
    /**
     * 昵称
     */
    private String nickname;
    /**
     * 密码
     */
    private String password;
    /**
     * 账号
     */
    private String username;
    /**
     * token 版本号：改密码后 +1，使此前签发的 token 全部失效
     */
    private Integer tokenVersion;
}
