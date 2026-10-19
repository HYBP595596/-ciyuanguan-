package com.dimension.vo;

import com.dimension.entity.Comment;
import com.dimension.entity.Image;
import com.dimension.entity.User;
import lombok.Data;

@Data
public class CommentUserImage {
    private Comment comment;
    private User user;
    private Image image;
}
