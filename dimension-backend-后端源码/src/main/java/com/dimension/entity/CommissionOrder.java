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
    private Integer status; // 0待支付, 1绘制中, 2待验收, 3已完成
    private String deliveryPreview;
    private String deliveryFinal;
    private Date createTime;
    private Date updateTime;
    private String transactionId;
}