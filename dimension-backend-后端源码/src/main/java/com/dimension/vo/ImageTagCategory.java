package com.dimension.vo;

import com.dimension.entity.Image;
import com.dimension.entity.ImageCategory;
import com.dimension.entity.Tag;
import lombok.Data;

import java.util.List;

@Data
public class ImageTagCategory {
    private Image image;
    private List<Tag> tagList;
    private ImageCategory category;
}
