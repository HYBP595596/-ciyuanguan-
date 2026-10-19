package com.dimension.service.impl;

import com.dimension.entity.Image;
import com.dimension.entity.ImageCategory;
import com.dimension.entity.Tag;
import com.dimension.entity.WatchHistory;
import com.dimension.mapper.WatchHistoryMapper;
import com.dimension.service.WatchHistoryService;
import com.dimension.utils.SessionUtil;
import com.dimension.vo.ImageHistoryCategory;
import com.dimension.vo.ImageTagHistoryCategory;

import java.util.ArrayList;
import java.util.List;

public class WatchHistoryServiceImpl implements WatchHistoryService {
    private WatchHistoryMapper watchHistoryMapper = SessionUtil.getMapper(WatchHistoryMapper.class);

    @Override
    public void addImageWatchHistory(Long imageid, Long userid) {
        watchHistoryMapper.addImageWatchHistory(imageid,userid);
    }

    @Override
    public List<ImageTagHistoryCategory> getUserWatchHistory(Long userid, Integer pageNum, Integer pageSize) {
        // 1.创建最终数据
        List<ImageTagHistoryCategory> imageTagHistoryList = new ArrayList<>();

        // 2.调用mapper封装最终数据

        List<ImageHistoryCategory> ImageTagHistoryCategoryList = watchHistoryMapper.getUserWatchHistory(userid,pageNum,pageSize);
        for (ImageHistoryCategory imageHistoryCategory : ImageTagHistoryCategoryList) {
            Image image = imageHistoryCategory.getImage();
            WatchHistory watchHistory = imageHistoryCategory.getWatchHistory();
            ImageCategory imageCategory = imageHistoryCategory.getCategory();

            List<Tag> tagList = watchHistoryMapper.getTagList(image.getId());
            ImageTagHistoryCategory imageTagHistoryCategoryList = new ImageTagHistoryCategory();

            imageTagHistoryCategoryList.setImage(image);
            imageTagHistoryCategoryList.setWatchHistory(watchHistory);
            imageTagHistoryCategoryList.setCategory(imageCategory);
            imageTagHistoryCategoryList.setTagList(tagList);

            imageTagHistoryList.add(imageTagHistoryCategoryList);
        }


        // 3.返回数据
        return imageTagHistoryList;
    }

    @Override
    public void removeWatchHistoryById(Long id) {
        watchHistoryMapper.removeWatchHistoryById(id);
    }

    @Override
    public long getTotal(Long userid) {
        return watchHistoryMapper.getTotal(userid);
    }
}
