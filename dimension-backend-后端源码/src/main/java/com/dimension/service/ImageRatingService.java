package com.dimension.service;

import com.dimension.entity.ImageRating;

public interface ImageRatingService {
    ImageRating getUserRate(Long userId, Long imageId);

    void setLike(Long userId, Long imageId);

    void setDislike(Long userId, Long imageId);

    void setCancelImageRate(Long userId, Long imageId);

    Long getLikes(Long imageId);

    Long getDislikes(Long imageId);
}
