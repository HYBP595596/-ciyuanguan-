package com.dimension.mapper;

import com.dimension.entity.ArtistBalance;
import com.dimension.entity.ArtistWithdraw;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/** 画师余额与提现（资金侧，和订单状态机分开维护） */
public interface ArtistBalanceMapper {

    ArtistBalance getByUserId(@Param("userId") Long userId);

    /** 订单验收后给画师加钱：没有记录就建一条 */
    int addIncome(@Param("userId") Long userId, @Param("amount") BigDecimal amount);

    /** 提现申请冻结/扣减：余额不足则影响 0 行（数据库层面防超提） */
    int deductBalance(@Param("userId") Long userId, @Param("amount") BigDecimal amount);

    /** 驳回时把钱退回余额 */
    int refundBalance(@Param("userId") Long userId, @Param("amount") BigDecimal amount);

    int addWithdrawn(@Param("userId") Long userId, @Param("amount") BigDecimal amount);

    // ---------- 提现申请 ----------
    int insertWithdraw(ArtistWithdraw withdraw);

    ArtistWithdraw getWithdrawById(@Param("id") Long id);

    List<ArtistWithdraw> getWithdrawsByUser(@Param("userId") Long userId);

    List<ArtistWithdraw> getWithdraws(@Param("status") Integer status,
                                      @Param("pageNum") Integer pageNum,
                                      @Param("pageSize") Integer pageSize);

    long getWithdrawTotal(@Param("status") Integer status);

    /** 审核：只有"申请中"能被改，避免重复处理 */
    int auditWithdraw(@Param("id") Long id,
                      @Param("status") Integer status,
                      @Param("adminId") Long adminId,
                      @Param("remark") String remark);

    int countPendingByUser(@Param("userId") Long userId);
}
