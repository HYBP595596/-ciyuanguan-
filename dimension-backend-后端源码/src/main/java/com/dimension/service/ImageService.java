package com.dimension.service;

import com.dimension.vo.ImageTagCategory;
import com.dimension.vo.ImageTagTZP;
import com.dimension.entity.Image;

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

    void addImage(String url, Integer categoryId, List<Object> rawTags, Long userId, String extraImages, String txtUrl);

    void computeImageScore(Long imageId, Integer likes, Integer dislikes);

    List<ImageTagTZP> getImageTagList(Integer pageNum, Integer pageSize);


    long getTotalWithNoCondition();

    List<ImageTagTZP> getImageTagListWithCondition(String keyword, Integer pageNum, Integer pageSize);

    long getTotalWithCondition(String keyword);

    List<ImageTagTZP> getImageListWithCategoryId(Integer categoryId, Integer pageNum, Integer pageSize);

    ImageTagTZP GetImageDetailById(long imageId);

    /** 按 id 取作品（权限校验用） */
    Image getImageById(Long imageId);

    List<ImageTagTZP> getRecommendImageList(Integer categoryId, long imageId);


    void increaseImageViewsIncrement(long imageId);

    void increaseImageVotesIncrement (long imageId);

    void imageCommentsDecrement(Long imageId);

    long getCategoryImageTotal(Integer categoryId);

    /** 后台分类管理：某分类下的作品（含标签） */
    List<ImageTagTZP> getAdminImagesByCategory(Integer categoryId, Integer pageNum, Integer pageSize);

    long getAdminCategoryImageTotal(Integer categoryId);

    /** 把作品移到另一个分类（只改分类，不动标签） */
    boolean moveImageCategory(Long imageId, Integer categoryId);

    List<Image> getPendingImageList(Integer pageNum, Integer pageSize);

    /** 待审核列表（带标签），后台审核页用 */
    List<ImageTagTZP> getPendingImageTagList(Integer pageNum, Integer pageSize);

    long getPendingTotal();

    void auditImage(Long imageId, Integer status);

}
