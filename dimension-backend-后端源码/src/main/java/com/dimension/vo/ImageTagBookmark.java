package com.dimension.vo;

import com.dimension.entity.Image;
import com.dimension.entity.ImageBookmark;
import com.dimension.entity.Tag;
import lombok.Data;

import java.util.List;
@Data
public class ImageTagBookmark {
    private Image image;
    private List<Tag> tagList;
    private ImageBookmark imageBookmark;
}
