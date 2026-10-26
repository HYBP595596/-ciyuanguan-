package com.dimension.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/** 画师提现申请：0 申请中 / 1 已打款 / 2 已驳回 */
@Data
public class ArtistWithdraw {
    private Long id;
    private Long userId;
    private BigDecimal amount;
    private Integer status;
    private String account;   // 收款方式说明（支付宝账号等）
    private String remark;    // 驳回原因/备注
    private Long auditAdminId;
    private Date createTime;
    private Date updateTime;

    // 非表字段：展示用
    private String nickname;
    private String avatar;
}
