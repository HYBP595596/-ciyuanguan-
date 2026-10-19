package com.dimension.vo;

import com.dimension.entity.Image;
import com.dimension.entity.ImageCategory;
import com.dimension.entity.Tag;
import com.dimension.entity.WatchHistory;
import lombok.Data;

import java.util.List;
@Data
public class ImageTagHistoryCategory {
    private Image image;
    private WatchHistory watchHistory;
    private ImageCategory category;
    private List<Tag> tagList;

}
