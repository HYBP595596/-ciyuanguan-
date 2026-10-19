package com.dimension.service.impl;

import com.dimension.mapper.CommentMapper;
import com.dimension.service.CommentService;
import com.dimension.utils.SessionUtil;
import com.dimension.vo.CommentUser;
import com.dimension.vo.CommentUserImage;

import java.util.List;

public class CommentServiceImpl implements CommentService {
    private CommentMapper commentMapper = SessionUtil.getMapper(CommentMapper.class);

    @Override
    public void addCommentImage(String content, Long imageId, Long userId) {
        commentMapper.addCommentImage(content,imageId,userId);

    }

    @Override
    public void removecomment(Long commentid) {
        commentMapper.removeComment(commentid);
    }

    @Override
    public List<CommentUser> getCommentUser(Long imageId, Integer pageNum, Integer pageSize){
        List<CommentUser> commentUserList=commentMapper.getCommentUser(imageId,pageNum,pageSize);
        return  commentUserList;
    }

    @Override
    public List<CommentUserImage> getCommentUserImage(Integer pageNum, Integer pageSize) {
        List<CommentUserImage> commentUserImageList=commentMapper.getCommentUserImage(pageNum,pageSize);
        return commentUserImageList;
    }

    @Override
    public long getCommentListTotal(Long imageId) {
        return commentMapper.getCommentListTotal(imageId);
    }

    @Override
    public long getTotalWithNoCondition() {
        return commentMapper.getTotalWithNoCondition();
    }

    @Override
    public List<CommentUserImage> getCommentTableWithCondition(String nickname, Integer pageNum, Integer pageSize) {
        List<CommentUserImage> commentUserImageList = commentMapper.getCommentTableWithCondition(nickname,pageNum,pageSize);

        return commentUserImageList;
    }

    @Override
    public Long gettotal(String nickname) {
        Long total = commentMapper.gettotal(nickname);
        return total;
    }
}
