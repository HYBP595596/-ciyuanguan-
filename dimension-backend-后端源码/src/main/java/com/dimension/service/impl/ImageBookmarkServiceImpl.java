package com.dimension.service.impl;

import com.dimension.entity.Image;
import com.dimension.entity.ImageBookmark;
import com.dimension.entity.Tag;
import com.dimension.mapper.ImageBookmarkMapper;
import com.dimension.service.ImageBookmarkService;
import com.dimension.utils.SessionUtil;
import com.dimension.vo.ImageBookmarkList;
import com.dimension.vo.ImageTagBookmark;

import java.util.ArrayList;
import java.util.List;

public class ImageBookmarkServiceImpl implements ImageBookmarkService {
    private ImageBookmarkMapper imageBookmarkMapper = SessionUtil.getMapper(ImageBookmarkMapper.class);

    @Override
    public List<ImageBookmark> getimageBookmark(Long userId,Long imageId) {
        List<ImageBookmark> list = imageBookmarkMapper.getimageBookmark(userId,imageId);
        return list;
    }

    @Override
    public void deleteImageBookmark(Long userId, Long imageId) {
        imageBookmarkMapper.deleteImageBookmark(userId,imageId);
        // 且该图片收藏数-1
        imageBookmarkMapper.imageBookmarkDecrement(imageId);
    }

    @Override
    public void insertImageBookmark(Long userId, Long imageId) {
        imageBookmarkMapper.insertImageBookmark(userId,imageId);
        // 该图片收藏数+1
        imageBookmarkMapper.imageBookmarkIncrement(imageId);
    }

    @Override
    public List<ImageTagBookmark> getUserImageBookmarkList(Integer userId, Integer pageNum, Integer pageSize) {
        List<ImageTagBookmark> imageTagBookmarkList = new ArrayList<>();

        List<ImageBookmarkList> imageBookmarkList = imageBookmarkMapper.getUserImageBookmarkList(userId,pageNum,pageSize);
        for (ImageBookmarkList imageBookmark : imageBookmarkList) {
            Image image = imageBookmark.getImage();
            ImageBookmark imageBookmark1 = imageBookmark.getImageBookmark();

            List<Tag> tagList = imageBookmarkMapper.getTagList(image.getId());

            ImageTagBookmark imageTagBookmarkList2 = new ImageTagBookmark();

            imageTagBookmarkList2.setImage(image);
            imageTagBookmarkList2.setImageBookmark(imageBookmark1);
            imageTagBookmarkList2.setTagList(tagList);

            imageTagBookmarkList.add(imageTagBookmarkList2);
        }

        return imageTagBookmarkList;
    }

    @Override
    public Long getCounttotal(Integer userId) {
        Long total = imageBookmarkMapper.getCounttotal(userId);
        return total;
    }
}
