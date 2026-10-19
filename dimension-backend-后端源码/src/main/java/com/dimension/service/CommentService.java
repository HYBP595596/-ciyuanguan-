package com.dimension.service;

import com.dimension.vo.CommentUser;
import com.dimension.vo.CommentUserImage;

import java.util.List;

public interface CommentService {
    List<CommentUserImage> getCommentTableWithCondition(String nickname, Integer pageNum, Integer pageSize);

    Long gettotal(String nickname);

    void addCommentImage(String content, Long imageId, Long userId);

    void removecomment(Long commentid);

    List<CommentUser> getCommentUser(Long imageId, Integer pageNum, Integer pageSize);

    List<CommentUserImage> getCommentUserImage(Integer pageNum,Integer pageSize);

    long getCommentListTotal(Long imageId);

    long getTotalWithNoCondition();
}
