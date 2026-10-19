package com.dimension.vo;

import com.dimension.entity.Image;
import com.dimension.entity.ImageCategory;
import com.dimension.entity.WatchHistory;
import lombok.Data;

@Data
public class ImageHistoryCategory {
    private Image image;
    private WatchHistory watchHistory;
    private ImageCategory category;
}
