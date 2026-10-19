package com.dimension.mapper;

import org.apache.ibatis.annotations.Param;

public interface FollowMapper {
    // 检查是否已关注
    int checkIsFollowing(@Param("userId") Long userId, @Param("followedUserId") Long followedUserId);
    
    // 添加关注
    void insertFollow(@Param("userId") Long userId, @Param("followedUserId") Long followedUserId);
    
    // 取消关注
    void deleteFollow(@Param("userId") Long userId, @Param("followedUserId") Long followedUserId);
    
    // 获取粉丝数
    int getFansCount(@Param("followedUserId") Long followedUserId);
}