package com.dimension.vo;

import com.dimension.entity.Image;
import com.dimension.entity.ImageBookmark;
import lombok.Data;

@Data
public class ImageBookmarkList {
    private Image image;
    private ImageBookmark imageBookmark;
}
