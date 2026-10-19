package com.dimension.service;

import com.dimension.entity.ImageBookmark;
import com.dimension.vo.ImageTagBookmark;

import java.util.List;

public interface ImageBookmarkService {
    List<ImageTagBookmark> getUserImageBookmarkList(Integer userId, Integer pageNum, Integer pageSize);

    Long getCounttotal(Integer userId);

    //根据图片id和用户id查询一条收藏记录
    List<ImageBookmark> getimageBookmark(Long userId, Long imageId);

    //根据图片id和用户id删除收藏记录
    void deleteImageBookmark(Long userId,Long imageId);

    //根据图片id和用户id添加收藏记录，时间为当前时间
    void insertImageBookmark(Long userId,Long imageId);
}
