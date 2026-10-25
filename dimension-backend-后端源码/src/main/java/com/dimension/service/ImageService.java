package com.dimension.service;

import com.dimension.vo.ImageTagCategory;
import com.dimension.vo.ImageTagTZP;

import java.util.List;

public interface ImageService {
    long getTotalWithoutCondition();

    void decreaseImageVotesByImageId(long imageId);

    void increaseImageCommentsByImageId(long imageId);

    List<ImageTagCategory> getImageTagCategoryList(Integer pageNum, Integer pageSize);

    List<ImageTagCategory> getImageTagCategoryListByKeyword(String keyword, Integer pageNum, Integer pageSize);

    long getTotalByKeyword(String keyword);

    void removeImageById(long imageId);

    void updateImageTagsAndCategory(Integer categoryId, List<Integer> tagIds, Long imageId);

    void addImage(String url, Integer categoryId, List<Object> rawTags, Long userId);

    void computeImageScore(Long imageId, Integer likes, Integer dislikes);

    List<ImageTagTZP> getImageTagList(Integer pageNum, Integer pageSize);


    long getTotalWithNoCondition();

    List<ImageTagTZP> getImageTagListWithCondition(String keyword, Integer pageNum, Integer pageSize);

    long getTotalWithCondition(String keyword);

    List<ImageTagTZP> getImageListWithCategoryId(Integer categoryId, Integer pageNum, Integer pageSize);

    ImageTagTZP GetImageDetailById(long imageId);

    List<ImageTagTZP> getRecommendImageList(Integer categoryId, long imageId);


    void increaseImageViewsIncrement(long imageId);

    void increaseImageVotesIncrement (long imageId);

    void imageCommentsDecrement(Long imageId);

    long getCategoryImageTotal(Integer categoryId);

}
