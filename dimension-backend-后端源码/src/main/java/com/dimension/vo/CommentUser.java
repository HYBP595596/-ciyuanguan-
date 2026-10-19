package com.dimension.vo;

import com.dimension.entity.Comment;
import com.dimension.entity.User;
import lombok.Data;

@Data
public class CommentUser {
    private Comment comment;
    private User user;
}
