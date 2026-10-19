package com.dimension.service.impl;

import com.dimension.entity.Image;
import com.dimension.entity.ImageCategory;
import com.dimension.entity.Tag;
import com.dimension.mapper.ImageMapper;
import com.dimension.service.ImageService;
import com.dimension.utils.SessionUtil;
import com.dimension.vo.ImageTagCategory;
import com.dimension.vo.ImageTagTZP;

import com.dimension.mapper.UserMapper;
import com.dimension.entity.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ImageServiceImpl implements ImageService {
    private ImageMapper imageMapper = SessionUtil.getMapper(ImageMapper.class);
    private UserMapper userMapper = SessionUtil.getMapper(UserMapper.class);

    @Override
    public List<ImageTagTZP> getImageTagList(Integer pageNum, Integer pageSize) {
        // 1.创建最终数据
        List<ImageTagTZP> imageTagList = new ArrayList<>();

        // 2.调用mapper封装最终数据
        List<Image> imageList = imageMapper.getImageListWithNoCondition(pageNum, pageSize);

        for (Image image : imageList) {
            List<Tag> tagList = imageMapper.getTagListWithNoCondition(image.getId());
            ImageTagTZP imageTag = new ImageTagTZP();
            imageTag.setImage(image);
            imageTag.setTagList(tagList);
            imageTagList.add(imageTag);
        }

        // 3.返回数据
        return imageTagList;
    }

    @Override
    public long getTotalWithNoCondition() {
        long total = imageMapper.getTotalWithNoCondition();

        return total;
    }


    @Override
    public List<ImageTagTZP> getImageTagListWithCondition(String keyword, Integer pageNum, Integer pageSize) {

        // 1.创建最终数据
        List<ImageTagTZP> imageTagList = new ArrayList<>();

        // 2.调用mapper封装最终数据
        List<Image> imageList = imageMapper.getImageListWithCondition(keyword, pageNum, pageSize);

        for (Image image : imageList) {
            List<Tag> tagList = imageMapper.getTagListWithCondition(image.getId());
            ImageTagTZP imageTag = new ImageTagTZP();
            imageTag.setImage(image);
            imageTag.setTagList(tagList);
            imageTagList.add(imageTag);
        }


        return imageTagList;
    }

    @Override
    public long getTotalWithCondition(String keyword) {
        long total = imageMapper.getTotalWithCondition(keyword);
        return total;
    }

    @Override
    public List<ImageTagTZP> getImageListWithCategoryId(Integer categoryId, Integer pageNum, Integer pageSize) {
        List<ImageTagTZP> imageTagList = new ArrayList<>();

        // 2. 调用mapper获取推荐图片列表
        List<Image> imageList = imageMapper.getImageListWithCategoryId(categoryId,pageNum,pageSize);

        // 3. 遍历图片列表，获取每张图片的标签列表
        for (Image image : imageList) {
            if (image != null) {
                List<Tag> tagList = imageMapper.getTagListWithCategoryId(image.getId());

                // 4. 创建ImageTag对象并设置图片和标签列表
                ImageTagTZP imageTag = new ImageTagTZP();
                imageTag.setImage(image);
                imageTag.setTagList(tagList);

                // 5. 将ImageTag对象添加到最终数据列表
                imageTagList.add(imageTag);
            }
        }

        // 6. 返回最终数据列表
        return imageTagList;
    }

        // 在类里面注入 UserMapper
    private UserMapper userMapper = SessionUtil.getMapper(UserMapper.class);

    @Override
    public ImageTagTZP GetImageDetailById(long imageId) {
        ImageTagTZP imageTag = new ImageTagTZP();

        Image image = imageMapper.getImageById(imageId);
        if (image != null) {
            List<Tag> tagList = imageMapper.getTagListByImageId(image.getId());
            imageTag.setImage(image);
            imageTag.setTagList(tagList);

            // 👇 新增：根据图片的 userId 查作者
            if (image.getUserId() != null) {
                User author = userMapper.getUserInfo(image.getUserId());
                imageTag.setUser(author);
                imageTag.setFansCount(0); // 粉丝数暂时填 0
            }
        }

        return imageTag;
    }

    @Override
    public List<ImageTagTZP> getRecommendImageList(Integer categoryId, long imageId) {
        List<ImageTagTZP> imageTagList = new ArrayList<>();

        // 2. 调用mapper获取推荐图片列表
        List<Image> imageList = imageMapper.getRecommendImageList(categoryId, imageId);

        // 3. 遍历图片列表，获取每张图片的标签列表
        for (Image image : imageList) {
            if (image != null) {
                List<Tag> tagList = imageMapper.getRecommendTagListByImageId(image.getId());

                // 4. 创建ImageTag对象并设置图片和标签列表
                ImageTagTZP imageTag = new ImageTagTZP();
                imageTag.setImage(image);
                imageTag.setTagList(tagList);

                // 5. 将ImageTag对象添加到最终数据列表
                imageTagList.add(imageTag);
            }
        }

        // 6. 返回最终数据列表
        return imageTagList;


    }

    @Override
    public void increaseImageViewsIncrement(long imageId) {
        imageMapper.increaseImageViewsIncrement(imageId);
    }

    @Override
    public void increaseImageVotesIncrement(long imageId) {
        imageMapper.increaseImageVotesIncrement(imageId);
    }

    @Override
    public void imageCommentsDecrement(Long imageId) {
        imageMapper.imageCommentsDecrement(imageId);
    }

    @Override
    public long getTotalWithoutCondition() {
        long total = imageMapper.getTotalWithoutCondition();
        return total;
    }

    @Override
    public void decreaseImageVotesByImageId(long imageId) {
        imageMapper.decreaseImageVotesByImageId(imageId);
    }

    @Override
    public void increaseImageCommentsByImageId(long imageId) {
        imageMapper.increaseImageCommentsByImageId(imageId);
    }

    @Override
    public List<ImageTagCategory> getImageTagCategoryList(Integer pageNum, Integer pageSize) {
        //创建最终数据
        ArrayList<ImageTagCategory> imageTagCategoryList = new ArrayList<>();

        //无条件分页查询图片表
        List<Image> imageList = imageMapper.getImageListWithoutCondition(pageNum,pageSize);

        for (Image image : imageList) {
            List<Tag> tagList = imageMapper.getTagListWithCondition(image.getId());                 //查询图片对应的标签列表
            ImageCategory imageCategory = imageMapper.getImageCategoryByImageId(image.getId());     //查询图片对应的图片类别
            ImageTagCategory imageTagCategory = new ImageTagCategory();
            imageTagCategory.setImage(image);
            imageTagCategory.setTagList(tagList);
            imageTagCategory.setCategory(imageCategory);
            imageTagCategoryList.add(imageTagCategory);
        }
        return imageTagCategoryList;
    }

    @Override
    public List<ImageTagCategory> getImageTagCategoryListByKeyword(String keyword, Integer pageNum, Integer pageSize) {
        //创建最终数据
        ArrayList<ImageTagCategory> imageTagCategoryList = new ArrayList<>();

        //根据标签关键词查询图片表
        keyword = "%"+keyword+"%";
        List<Image> imageList = imageMapper.getImageListByKeyword(keyword,pageNum,pageSize);

        for (Image image : imageList) {
            List<Tag> tagList = imageMapper.getTagListWithCondition(image.getId());                 //查询图片对应的标签列表
            ImageCategory imageCategory = imageMapper.getImageCategoryByImageId(image.getId());     //查询图片对应的图片类别
            ImageTagCategory imageTagCategory = new ImageTagCategory();
            imageTagCategory.setImage(image);
            imageTagCategory.setTagList(tagList);
            imageTagCategory.setCategory(imageCategory);
            imageTagCategoryList.add(imageTagCategory);
        }
        return imageTagCategoryList;
    }

    @Override
    public long getTotalByKeyword(String keyword) {
        keyword = "%"+keyword+"%";
        long total = imageMapper.getTotalByKeyword(keyword);
        return total;
    }

    @Override
    public void removeImageById(long imageId) {
        Image image = imageMapper.getImageById(imageId);             //获取图片
        imageMapper.removeCommentByImageId(imageId);                 //删除该图片下的所有评论
        imageMapper.removeBookmarkByImageId(imageId);                //删除该图片下的所有收藏信息
        imageMapper.removeImageRatingByImageId(imageId);             //删除该图片下的所有评分信息
        imageMapper.removeImageTagsRelationByImageId(imageId);       //删除图片标签关系表中关于该图片的记录
        imageMapper.removeWatchHistoryByImageId(imageId);            //删除该图片下的所有浏览历史信息
        imageMapper.removeImageById(imageId);                        //删除该图片
        imageMapper.categoryCountDecrement(image.getCategoryId());   //该分类下的图片数-1
    }



    @Override
    public void updateImageTagsAndCategory(Integer categoryId, List<Integer> tagIds, Long imageId) {
        imageMapper.updateImageCategoryByImageId(imageId,categoryId);        //修改图片的分类

        imageMapper.removeImageTagsRelationByImageId(imageId);               //删除图片标签关系表中关于该图片的记录
        for (Integer tagId : tagIds) {
            imageMapper.insertImageTagRelationByImageId(imageId,tagId);      //向图片标签关系中插入数据
        }
    }

    @Override
    public void addImage(String url, Integer categoryId, List<Integer> tagIds, Long userId) {
        Image image = new Image();                                         //封装数据
        image.setCategoryId(categoryId);
        image.setUrl(url);
        image.setScore(5);                                                 //图片初始分为5
        image.setCollects(0);
        image.setComments(0);
        image.setCreateTime(new Date());
        image.setVotes(0);
        image.setViews(0);
        image.setUserId(userId); // 👈 新增：保存上传者ID

        imageMapper.insertImage(image);                                    //插入图片
        long imageId = image.getId();                                      //获取插入图片的主键

        for (Integer tagId : tagIds) {                                     //往图片-标签表中添加记录
            imageMapper.insertImageTagRelationByImageId(imageId,tagId);
        }

        imageMapper.increaseCategoryCount(categoryId);                     //根据分类id将该分类的count数量+1
    }

    @Override
    public void computeImageScore(Long imageId, Integer likes, Integer dislikes) {
        // 获取图片评分
        float originScore = imageMapper.getImageById(imageId).getScore();

        float score = (float) (originScore + likes*0.1 - dislikes*0.1);   //计算评分，初始为5分,like权值为0.1，dislike权值为-0.1
        if(score < 0){                                          //限定评分范围
            score = 0;
        }else if(score > 10){
            score = 10;
        }

        imageMapper.updateImageScore(score, imageId);           //更新评分
    }
}
