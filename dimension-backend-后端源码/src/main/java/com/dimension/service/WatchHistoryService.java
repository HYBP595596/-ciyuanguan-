package com.dimension.service;

import com.dimension.vo.ImageTagHistoryCategory;

import java.util.List;

public interface WatchHistoryService {
    void addImageWatchHistory(Long imageid, Long userid);

    List<ImageTagHistoryCategory> getUserWatchHistory(Long userid, Integer pageNum, Integer pageSize);

    //根据id删除对应watchHistory记录
    void removeWatchHistoryById(Long id);

    long getTotal(Long userid);
}


