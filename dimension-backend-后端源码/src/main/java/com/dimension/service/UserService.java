package com.dimension.service;

import com.dimension.entity.User;

import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface UserService {
    List<User> getUserTableWithNoCondition(Integer pageNum, Integer pageSize);

    Long getTotalWithNoCondition();

    List<User> getUserTableWithUsername(String username);

    void forbidUser(Integer userId);

    void resumeUser(Integer userId);

    Map<String, Object> userLogin(String username, String password, HttpServletResponse resp);

    User getUserInfo(long userId);

    int updateUserAvatar(Long userId, String avatar);

    int updateUserInfo(Long userId, String nickname, String description, Integer sex, Date birthday);

    int updateUserPassword(Long userId, String password);

    int registerUser(String nickname, String username, String password);

    List<User> getUserTableWithNickname(Integer pageNum, Integer pageSize, String nickname);

    long getTotalWithNickname(String nickname);

    int updateUserCover(Long userId, String coverImage, Integer coverHeight);

    int applyArtist(Long userId, String desc);

    List<User> getPendingArtists(Integer pageNum, Integer pageSize);

    long getPendingArtistTotal();

    int auditArtist(Long userId, Integer status);


}
