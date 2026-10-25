package com.dimension.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class Transaction {
    private Long id;
    private String orderNo;
    private Long buyerId;
    private Long artistId;
    private BigDecimal amount;
    private String type; // PAY, INCOME, REFUND
    private String transactionId;
    private Date createTime;
}