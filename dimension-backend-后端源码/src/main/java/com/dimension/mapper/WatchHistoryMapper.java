package com.dimension.mapper;

import com.dimension.entity.Tag;
import com.dimension.vo.ImageHistoryCategory;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface WatchHistoryMapper {
    void addImageWatchHistory(@Param("image_id") Long imageid,
                              @Param("user_id") Long userid);

    List<ImageHistoryCategory> getUserWatchHistory(@Param("user_id") Long userid,
                                                   @Param("pageNum") Integer pageNum,
                                                   @Param("pageSize") Integer pageSize);
    List<Tag> getTagList(@Param("id") Long id);

    //根据id删除对应watchHistory记录
    void removeWatchHistoryById(@Param("id") Long id);

    long getTotal(@Param("userid") Long userid);
}
