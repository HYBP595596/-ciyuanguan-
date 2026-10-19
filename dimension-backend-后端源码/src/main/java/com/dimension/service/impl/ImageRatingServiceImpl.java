package com.dimension.service.impl;

import com.dimension.entity.ImageRating;
import com.dimension.mapper.ImageRatingMapper;
import com.dimension.service.ImageRatingService;
import com.dimension.utils.SessionUtil;

public class ImageRatingServiceImpl implements ImageRatingService {
    private ImageRatingMapper imageRatingMapper = SessionUtil.getMapper(ImageRatingMapper.class);

    @Override
    public ImageRating getUserRate(Long userId, Long imageId) {
        return imageRatingMapper.getUserRate(userId,imageId);
    }

    @Override
    public void setLike(Long userId, Long imageId) {
        // 先获取，如果有就执行修改，否则才执行添加
        ImageRating userRate = imageRatingMapper.getUserRate(userId, imageId);
        if (userRate == null) {
            imageRatingMapper.setLike(userId, imageId);
        } else {
            // 执行修改
            imageRatingMapper.updateList(userId, imageId);
        }
    }

    @Override
    public void setDislike(Long userId, Long imageId) {
        ImageRating userRate = imageRatingMapper.getUserRate(userId, imageId);
        if (userRate == null) {
            imageRatingMapper.setDislike(userId, imageId);
        } else {
            imageRatingMapper.updateDislike(userId, imageId);
        }
    }

    @Override
    public void setCancelImageRate(Long userId, Long imageId) {
        imageRatingMapper.setCancelImageRate(userId, imageId);
    }

    @Override
    public Long getLikes(Long imageId) {
        return imageRatingMapper.getLikes(imageId);
    }

    @Override
    public Long getDislikes(Long imageId) {
        return imageRatingMapper.getDislikes(imageId);
    }
}
