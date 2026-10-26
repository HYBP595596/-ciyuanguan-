package com.dimension.mapper;

import com.dimension.entity.ImageRating;
import org.apache.ibatis.annotations.Param;

public interface ImageRatingMapper {
    ImageRating getUserRate(@Param("userId")Long userId, @Param("imageId")Long imageId);

    void setLike(@Param("userId")Long userId, @Param("imageId")Long imageId);

    void setDislike(@Param("userId")Long userId, @Param("imageId")Long imageId);

    void setCancelImageRate(@Param("userId")Long userId, @Param("imageId")Long imageId);

    Long getLikes(@Param("imageId")Long imageId);

    Long getDislikes(@Param("imageId")Long imageId);

    void updateList(@Param("userId") Long userId, @Param("imageId") Long imageId);

    void updateDislike(@Param("userId") Long userId, @Param("imageId") Long imageId);

    void imageVotesIncrement(@Param("imageId") Long imageId);

    void imageVotesDecrement(@Param("imageId") Long imageId);

}
