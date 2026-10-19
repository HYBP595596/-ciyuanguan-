package com.dimension.service.impl;

import com.dimension.entity.ImageCategory;
import com.dimension.mapper.ImageCategoryMapper;
import com.dimension.service.ImageCategoryService;
import com.dimension.utils.SessionUtil;

import java.util.List;

public class ImageCategoryServiceImpl implements ImageCategoryService {
    private ImageCategoryMapper imageCategoryMapper = SessionUtil.getMapper(ImageCategoryMapper.class);

    @Override
    public List<ImageCategory> GetAllCategory() {
        List<ImageCategory> list = imageCategoryMapper.GetAllCategory();
        return list;
    }

}
