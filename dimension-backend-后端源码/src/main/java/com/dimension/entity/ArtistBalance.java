package com.dimension.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/** 画师余额（一人一行，验收结算时累加） */
@Data
public class ArtistBalance {
    private Long userId;
    private BigDecimal balance;       // 可提现余额
    private BigDecimal totalIncome;   // 累计实收
    private BigDecimal totalWithdrawn;// 累计已提现
    private Date updateTime;

    // 非表字段：展示用
    private String nickname;
    private String avatar;
}
