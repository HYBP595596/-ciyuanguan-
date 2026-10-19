package com.dimension.vo;

import com.dimension.entity.Image;
import com.dimension.entity.Tag;
import lombok.Data;

import java.util.List;

@Data
public class ImageTagXP {
    private Image image;
    private List<Tag> tagList;
    private Tag tag;
    private List<Image> imageList;
}
