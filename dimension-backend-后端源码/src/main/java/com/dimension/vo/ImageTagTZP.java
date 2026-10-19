package com.dimension.vo;

import com.dimension.entity.Image;
import com.dimension.entity.Tag;
import com.dimension.entity.User; // 👈 新增
import lombok.Data;

import java.util.List;

@Data
public class ImageTagTZP {
    private Image image;
    private List<Tag> tagList;
    private Tag tag;
    private List<Image> imageList;
    private User user;           // 👈 新增：装载作者信息
    private Integer fansCount;   // 👈 新增：装载粉丝数
}