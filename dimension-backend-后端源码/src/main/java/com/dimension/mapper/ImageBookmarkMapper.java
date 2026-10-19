package com.dimension.mapper;

import com.dimension.entity.ImageBookmark;
import com.dimension.entity.Tag;
import com.dimension.vo.ImageBookmarkList;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ImageBookmarkMapper {
    List<ImageBookmarkList> getUserImageBookmarkList(@Param("userId")Integer userId,
                                                     @Param("pageNum")Integer pageNum,
                                                     @Param("pageSize") Integer pageSize);

    List<Tag> getTagList(@Param("categoryId") Long categoryId);

    Long getCounttotal(@Param("userId") Integer userId);

    //根据图片id和用户id查询一条收藏记录
    List<ImageBookmark> getimageBookmark(@Param("userId") Long userId, @Param("imageId") Long imageId);

    //根据图片id和用户id删除收藏记录
    void deleteImageBookmark(@Param("userId") Long userId,@Param("imageId") Long imageId);

    //根据图片id和用户id添加收藏记录，时间为当前时间
    void insertImageBookmark(@Param("userId") Long userId,@Param("imageId") Long imageId);

    void imageBookmarkIncrement(@Param("imageId") Long imageId);

    void imageBookmarkDecrement(@Param("imageId") Long imageId);
}
