package com.dimension.mapper;

import com.dimension.entity.Image;
import com.dimension.entity.ImageCategory;
import com.dimension.entity.Tag;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ImageMapper {

    //peng
    long getTotalWithoutCondition();


    //peng
    void decreaseImageVotesByImageId(@Param("imageId") long imageId);

    //peng
    void increaseImageCommentsByImageId(@Param("imageId") long imageId);


    //peng
    List<Image> getImageListWithoutCondition(@Param("pageNum") Integer pageNum,
                                             @Param("pageSize") Integer pageSize);

    //peng
    ImageCategory getImageCategoryByImageId(@Param("imageId") Long imageId);


    //peng
    List<Image> getImageListByKeyword(@Param("keyword") String keyword,
                                      @Param("pageNum") Integer pageNum,
                                      @Param("pageSize") Integer pageSize);

    //peng
    long getTotalByKeyword(@Param("keyword") String keyword);


    //peng
    void removeImageById(@Param("imageId") Long imageId);

    //peng
    void removeCommentByImageId(@Param("imageId") Long imageId);

    //peng
    void removeBookmarkByImageId(@Param("imageId") Long imageId);

    //peng
    void removeImageRatingByImageId(@Param("imageId") Long imageId);

    //peng
    void removeImageTagsRelationByImageId(@Param("imageId") Long imageId);

    //peng
    void removeWatchHistoryByImageId(@Param("imageId") Long imageId);

    //peng
    void updateImageCategoryByImageId(@Param("imageId") Long imageId, @Param("categoryId") Integer categoryId);

    //peng
    void insertImageTagRelationByImageId(@Param("imageId") Long imageId, @Param("tagId") Integer tagId);

    //peng
    void insertImage(Image image);

    //peng
    void increaseCategoryCount(@Param("categoryId") Integer categoryId);

    void updateImageScore(@Param("score") float score,@Param("imageId") Long imageId);

    void imageCommentsDecrement(@Param("imageId") Long imageId);

    List<Image> getImageListWithNoCondition(@Param("pageNum") Integer pageNum,
                                            @Param("pageSize") Integer pageSize);

    List<Tag> getTagListWithNoCondition(@Param("id") Long id);

    long getTotalWithNoCondition();

    List<Image> getImageListWithCondition(@Param("keyword") String keyword, @Param("pageNum") Integer pageNum, @Param("pageSize") Integer pageSize);

    List<Tag> getTagListWithCondition(@Param("id") Long id);

    long getTotalWithCondition(@Param("keyword") String keyword);

    Image getImageById(@Param("imageId") Long imageId);

    List<Tag> getTagListByImageId(@Param("id") Long id);

    List<Image> getRecommendImageList(@Param("categoryId") Integer categoryId, @Param("imageId") long imageId);

    List<Tag> getRecommendTagListByImageId(@Param("id") Long id);

    List<Image> getImageListWithCategoryId(@Param("categoryId") Integer categoryId, @Param("pageNum") Integer pageNum,  @Param("pageSize") Integer pageSize);

    List<Tag> getTagListWithCategoryId(@Param("id") Long id);

    void increaseImageViewsIncrement(@Param("imageId") Long imageId);

    void increaseImageVotesIncrement(@Param("imageId") long imageId);

    void categoryCountDecrement(@Param("categoryId") long categoryId);

    List<Image> getUserImageList(@Param("userId") Long userId);

    long getCategoryImageTotal(@Param("categoryId") Integer categoryId);

    /** 后台分类管理：某分类下的全部作品（含待审核/驳回） */
    List<Image> getAdminImagesByCategory(@Param("categoryId") Integer categoryId,
                                         @Param("pageNum") Integer pageNum,
                                         @Param("pageSize") Integer pageSize);

    long getAdminCategoryImageTotal(@Param("categoryId") Integer categoryId);

    /** 按「已通过审核」的实际数量重算分类计数，保证迁移后计数准确 */
    void recalcCategoryCount(@Param("categoryId") Integer categoryId);

    List<Image> getPendingImageList(@Param("pageNum") Integer pageNum, @Param("pageSize") Integer pageSize);

    long getPendingTotal();

    void auditImage(@Param("imageId") Long imageId, @Param("status") Integer status);

}
