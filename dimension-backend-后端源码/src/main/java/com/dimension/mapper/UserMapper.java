package com.dimension.mapper;

import com.dimension.entity.User;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface UserMapper {

    User userLogin(@Param("username") String username,
                   @Param("password") String password);

    User getUserInfo(@Param("userId") Long userId);

    int updateUserAvatar(@Param("userId") Long userId,
                         @Param("avatar") String avatar);

    int updateUserInfo(@Param("userId") Long userId,
                       @Param("nickname") String nickname,
                       @Param("description") String description,
                       @Param("sex") Integer sex,
                       @Param("birthday") Date birthDay);

    int updateUserPassword(@Param("userId") Long userId,
                           @Param("password") String password);

    int isExistByNickname(@Param("nickname") String nickname);

    int isExistByUsername(@Param("username") String username);

    int addRegisterUser(@Param("user") User user);

    List<User> getUserTableWithUsername(@Param("username") String username);

    List<User> getUserTableWithNoCondition(@Param("pageNum") Integer pageNum, @Param("pageSize") Integer pageSize);

    Long totalList();

    List<User> getUserTableWithNickname(@Param("pageNum") Integer pageNum,
                                        @Param("pageSize") Integer pageSize,
                                        @Param("nickname") String nickname);

    long getTotalWithNickname(@Param("nickname") String nickname);

    void forbidUser(@Param("userId") Integer userId);

    void resumeUser(@Param("userId")Integer userId);

    int updateUserCover(@Param("userId") Long userId, @Param("coverImage") String coverImage,@Param("coverHeight") Integer coverHeight);

    int applyArtist(@Param("userId") Long userId, @Param("desc") String desc);

    List<User> getPendingArtists(@Param("pageNum") Integer pageNum, @Param("pageSize") Integer pageSize);

    long getPendingArtistTotal();

    int auditArtist(@Param("userId") Long userId, @Param("status") Integer status);

    /**
     * 注销账号（软注销）：status 置为 2，密码/昵称脱敏，画师身份与接稿开关一并关闭
     */
    int cancelAccount(@Param("userId") Long userId,
                      @Param("password") String password,
                      @Param("nickname") String nickname);

}
