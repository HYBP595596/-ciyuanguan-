package com.dimension.mapper;

import com.dimension.entity.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface FollowMapper {
    // 检查是否已关注
    int checkIsFollowing(@Param("userId") Long userId, @Param("followedUserId") Long followedUserId);

    // 添加关注
    void insertFollow(@Param("userId") Long userId, @Param("followedUserId") Long followedUserId);

    // 取消关注
    void deleteFollow(@Param("userId") Long userId, @Param("followedUserId") Long followedUserId);

    // 获取粉丝数
    int getFansCount(@Param("followedUserId") Long followedUserId);

    // 我关注了多少人
    int getFollowingCount(@Param("userId") Long userId);

    // 我关注的人（个人中心展示）
    List<User> getFollowingUsers(@Param("userId") Long userId);

    // 我的粉丝（个人中心展示）
    List<User> getFansUsers(@Param("userId") Long userId);
}
