package com.dimension.mapper;

import com.dimension.entity.Notice;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface NoticeMapper {

    int insertNotice(Notice notice);

    List<Notice> getNoticeList(@Param("pageNum") Integer pageNum, @Param("pageSize") Integer pageSize);

    long getNoticeTotal();

    List<Notice> getLatestNotices(@Param("limit") Integer limit);

    Notice getNoticeById(@Param("id") Long id);

    int deleteNotice(@Param("id") Long id);
}
