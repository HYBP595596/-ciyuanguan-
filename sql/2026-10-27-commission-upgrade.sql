-- ============================================================================
-- 约稿购买体系补强（全部在现有 commission_order / payment / transaction 上改）
-- 1. 订单补：平台抽成、画师实收、关闭时间
-- 2. 新增：画师余额表、提现申请表（现有代码里完全没有的业务）
-- 3. 索引：超时扫描、按订单查支付单
-- 执行：mysql -uroot -p dimension < 2026-10-27-commission-upgrade.sql
-- ============================================================================

USE dimension;

-- ---------- 1. 订单表补列 ----------
ALTER TABLE dimension_commission_order
    ADD COLUMN platform_fee  DECIMAL(10, 2) DEFAULT NULL COMMENT '平台抽成金额' AFTER transaction_id,
    ADD COLUMN artist_income DECIMAL(10, 2) DEFAULT NULL COMMENT '画师实收（应付金额-抽成）' AFTER platform_fee,
    ADD COLUMN close_time    DATETIME       DEFAULT NULL COMMENT '订单关闭时间（超时/取消）' AFTER artist_income;

-- status 语义补一位：5 = 已关闭（超时未支付 or 用户主动取消）
ALTER TABLE dimension_commission_order
    MODIFY COLUMN status INT DEFAULT 0 COMMENT '0待支付 1绘制中 2待验收 3已完成 4已退款 5已关闭';

-- ---------- 2. 索引 ----------
ALTER TABLE dimension_commission_order
    ADD INDEX idx_status_create (status, create_time);

ALTER TABLE dimension_payment
    ADD INDEX idx_out_trade_status (out_trade_no, status);

-- ---------- 3. 画师余额 ----------
CREATE TABLE IF NOT EXISTS dimension_artist_balance
(
    user_id         BIGINT PRIMARY KEY COMMENT '画师用户 id',
    balance         DECIMAL(12, 2) NOT NULL DEFAULT 0 COMMENT '可提现余额',
    total_income    DECIMAL(12, 2) NOT NULL DEFAULT 0 COMMENT '累计实收（订单验收后）',
    total_withdrawn DECIMAL(12, 2) NOT NULL DEFAULT 0 COMMENT '累计已提现',
    update_time     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT '画师余额';

-- ---------- 4. 提现申请 ----------
CREATE TABLE IF NOT EXISTS dimension_artist_withdraw
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id        BIGINT         NOT NULL,
    amount         DECIMAL(12, 2) NOT NULL,
    status         INT            NOT NULL DEFAULT 0 COMMENT '0申请中 1已打款 2已驳回',
    account        VARCHAR(100)            DEFAULT NULL COMMENT '收款账号/说明',
    remark         VARCHAR(200)            DEFAULT NULL COMMENT '驳回原因或备注',
    audit_admin_id BIGINT                  DEFAULT NULL,
    create_time    DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time    DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_user (user_id),
    KEY idx_status (status)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT '画师提现申请';

-- ---------- 5. 历史数据兜底（订单金额已有，抽成留空表示未结算） ----------
UPDATE dimension_commission_order
SET platform_fee  = 0.00,
    artist_income = amount
WHERE status = 3
  AND artist_income IS NULL;

-- ---------- 6. 统一排序规则 ----------
-- 现象：dimension_payment 是 utf8mb4_0900_ai_ci，而 dimension_commission_order 是
--       utf8mb4_general_ci，两者按 out_trade_no/order_no 做 JOIN 会直接报
--       "Illegal mix of collations"（对账 SQL 一写就炸）。
-- 处理：把资金侧的几张表统一到和订单表一致的 general_ci。
ALTER TABLE dimension_payment
    CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE dimension_artist_balance
    CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE dimension_artist_withdraw
    CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
