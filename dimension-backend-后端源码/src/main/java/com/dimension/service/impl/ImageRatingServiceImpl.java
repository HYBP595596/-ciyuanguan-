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
        ImageRating userRate = imageRatingMapper.getUserRate(userId, imageId);
        if (userRate == null) {
            imageRatingMapper.setLike(userId, imageId);
            imageRatingMapper.imageVotesIncrement(imageId);            // 新增点赞
        } else if (userRate.getLike() == 1) {
            imageRatingMapper.updateList(userId, imageId);              // 踩 → 赞
            imageRatingMapper.imageVotesIncrement(imageId);
        } else {
            // 已经是赞，再次点 → 取消
            imageRatingMapper.setCancelImageRate(userId, imageId);
            imageRatingMapper.imageVotesDecrement(imageId);
        }
    }

    @Override
    public void setDislike(Long userId, Long imageId) {
        ImageRating userRate = imageRatingMapper.getUserRate(userId, imageId);
        if (userRate == null) {
            imageRatingMapper.setDislike(userId, imageId);              // 新增踩，votes 不变
        } else if (userRate.getLike() == 0) {
            imageRatingMapper.updateDislike(userId, imageId);           // 赞 → 踩，votes 减 1
            imageRatingMapper.imageVotesDecrement(imageId);
        } else {
            imageRatingMapper.setCancelImageRate(userId, imageId);      // 取消踩
        }
    }

    @Override
    public void setCancelImageRate(Long userId, Long imageId) {
        ImageRating userRate = imageRatingMapper.getUserRate(userId, imageId);
        if (userRate != null && userRate.getLike() == 0) {
            imageRatingMapper.imageVotesDecrement(imageId);             // 取消的原本是赞
        }
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
