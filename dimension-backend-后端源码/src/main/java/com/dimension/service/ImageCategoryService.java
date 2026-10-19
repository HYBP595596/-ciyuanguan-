package com.dimension.service;

import com.dimension.entity.ImageCategory;

import java.util.List;

public interface ImageCategoryService {
    //查询所有分类，并根据sort升序排序
    List<ImageCategory> GetAllCategory();
}
