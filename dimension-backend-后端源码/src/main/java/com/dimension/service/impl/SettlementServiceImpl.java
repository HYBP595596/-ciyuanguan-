package com.dimension.service.impl;

import com.dimension.entity.ArtistBalance;
import com.dimension.entity.ArtistWithdraw;
import com.dimension.mapper.ArtistBalanceMapper;
import com.dimension.service.SettlementService;
import com.dimension.utils.SessionUtil;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SettlementServiceImpl implements SettlementService {

    private final ArtistBalanceMapper mapper = SessionUtil.getMapper(ArtistBalanceMapper.class);

    /** 最低提现金额（元） */
    private static final BigDecimal MIN_WITHDRAW = new BigDecimal("1.00");

    @Override
    public ArtistBalance getBalance(Long userId) {
        ArtistBalance balance = mapper.getByUserId(userId);
        if (balance != null) {
            return balance;
        }
        // 没有记录也返回一个 0，前端不用判空
        ArtistBalance empty = new ArtistBalance();
        empty.setUserId(userId);
        empty.setBalance(BigDecimal.ZERO);
        empty.setTotalIncome(BigDecimal.ZERO);
        empty.setTotalWithdrawn(BigDecimal.ZERO);
        return empty;
    }

    @Override
    public Map<String, Object> applyWithdraw(Long userId, BigDecimal amount, String account) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (userId == null) {
            result.put("flag", 1);
            result.put("message", "请先登录");
            return result;
        }
        if (amount == null || amount.compareTo(MIN_WITHDRAW) < 0) {
            result.put("flag", 1);
            result.put("message", "提现金额不能低于 " + MIN_WITHDRAW.toPlainString() + " 元");
            return result;
        }
        if (amount.scale() > 2) {
            result.put("flag", 1);
            result.put("message", "提现金额最多两位小数");
            return result;
        }

        Long withdrawId = SessionUtil.inTransaction(session -> {
            ArtistBalanceMapper bm = session.getMapper(ArtistBalanceMapper.class);
            // 同一时间只允许一笔在处理，避免重复冻结
            if (bm.countPendingByUser(userId) > 0) {
                return null;
            }
            // 数据库层面扣余额：余额不足会返回 0 行，避免并发超提
            int deducted = bm.deductBalance(userId, amount);
            if (deducted == 0) {
                return null;
            }
            ArtistWithdraw withdraw = new ArtistWithdraw();
            withdraw.setUserId(userId);
            withdraw.setAmount(amount);
            withdraw.setAccount(account);
            bm.insertWithdraw(withdraw);
            return withdraw.getId();
        });

        if (withdrawId == null) {
            result.put("flag", 1);
            result.put("message", "提现申请失败：余额不足，或已有一笔提现在审核中");
            return result;
        }
        result.put("flag", 0);
        result.put("withdrawId", withdrawId);
        result.put("message", "提现申请已提交，等待管理员审核");
        result.put("balance", getBalance(userId));
        return result;
    }

    @Override
    public List<ArtistWithdraw> listMine(Long userId) {
        return mapper.getWithdrawsByUser(userId);
    }

    @Override
    public List<ArtistWithdraw> listAll(Integer status, Integer pageNum, Integer pageSize) {
        return mapper.getWithdraws(status, pageNum, pageSize);
    }

    @Override
    public long countAll(Integer status) {
        return mapper.getWithdrawTotal(status);
    }

    @Override
    public boolean audit(Long withdrawId, Integer status, Long adminId, String remark) {
        if (withdrawId == null || status == null || (status != 1 && status != 2)) {
            return false;
        }
        return Boolean.TRUE.equals(SessionUtil.inTransaction(session -> {
            ArtistBalanceMapper bm = session.getMapper(ArtistBalanceMapper.class);
            ArtistWithdraw withdraw = bm.getWithdrawById(withdrawId);
            if (withdraw == null || withdraw.getStatus() == null || withdraw.getStatus() != 0) {
                return false; // 只审「申请中」
            }
            int rows = bm.auditWithdraw(withdrawId, status, adminId,
                    status == 2 ? (remark == null ? "管理员驳回" : remark) : remark);
            if (rows == 0) {
                return false; // 并发：别处已处理
            }
            if (status == 1) {
                // 打款成功：累计已提现 +1 笔（余额在申请时已扣）
                bm.addWithdrawn(withdraw.getUserId(), withdraw.getAmount());
            } else {
                // 驳回：余额退回
                bm.refundBalance(withdraw.getUserId(), withdraw.getAmount());
            }
            return true;
        }));
    }
}
