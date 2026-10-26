package com.dimension.service;

import com.dimension.entity.ArtistBalance;
import com.dimension.entity.ArtistWithdraw;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 画师结算：余额 + 提现。
 * 余额只由「订单验收」入账（CommissionTxOps.confirmComplete），这里负责查询与提现流程。
 */
public interface SettlementService {

    /** 余额（没有记录时返回 0 而不是 null，方便前端直接显示） */
    ArtistBalance getBalance(Long userId);

    /** 申请提现：校验余额、限制同时只能有一笔在处理 */
    Map<String, Object> applyWithdraw(Long userId, BigDecimal amount, String account);

    List<ArtistWithdraw> listMine(Long userId);

    List<ArtistWithdraw> listAll(Integer status, Integer pageNum, Integer pageSize);

    long countAll(Integer status);

    /**
     * 审核：status=1 已打款（累计已提现 +），status=2 已驳回（余额退回）。
     */
    boolean audit(Long withdrawId, Integer status, Long adminId, String remark);
}
