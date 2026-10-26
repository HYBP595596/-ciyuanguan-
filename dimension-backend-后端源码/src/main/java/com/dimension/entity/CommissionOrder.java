package com.dimension.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class CommissionOrder {
    private Long id;
    private String orderNo;
    private Long planId;
    private Long buyerId;
    private Long artistId;
    private String requirement;
    private BigDecimal amount;
    private Integer status; // 0待支付, 1绘制中, 2待验收, 3已完成, 4已退款, 5已关闭
    private Integer contentType; // 约稿类型: 1插画 2漫画 3小说
    private String deliveryPreview;
    private String deliveryFinal;
    private Date createTime;
    private Date updateTime;
    private String transactionId;
    private BigDecimal platformFee;   // 平台抽成（验收时写入）
    private BigDecimal artistIncome;  // 画师实收（验收时写入）
    private Date closeTime;           // 关闭时间（超时/取消）

    // 非表字段：列表展示用（联表带出）
    private String planTitle;
    private Integer deliveryDays;
    private String buyerNickname;
    private String buyerAvatar;
    private String artistNickname;
    private String artistAvatar;
}