package com.dimension.mapper;

import com.dimension.vo.CommentUser;
import com.dimension.vo.CommentUserImage;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CommentMapper {
    List<CommentUserImage> getCommentTableWithCondition(@Param("nickname") String nickname,
                                                        @Param("pageNum") Integer pageNum,
                                                        @Param("pageSize") Integer pageSize);

    Long gettotal(@Param("nickname")String nickname);

    void addCommentImage(@Param("content") String content,@Param("imageId") Long imageId,@Param("userId") Long userId);

    void removeComment(@Param("commentid") Long id);

    List<CommentUser> getCommentUser(@Param("imageId") Long imageId,
                                     @Param("pageNum") Integer pageNum,
                                     @Param("pageSize") Integer pageSize);
    List<CommentUserImage> getCommentUserImage(
            @Param("pageNum") Integer pageNum,
            @Param("pageSize") Integer pageSize
    );

    long getCommentListTotal(@Param("imageId") Long imageId);

    long getTotalWithNoCondition();
}
