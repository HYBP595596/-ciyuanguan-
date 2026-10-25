package com.dimension.service.impl;

import cn.hutool.json.JSONUtil;
import com.dimension.entity.User;
import com.dimension.mapper.UserMapper;
import com.dimension.service.UserService;
import com.dimension.utils.JwtUtil;
import com.dimension.utils.SessionUtil;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserServiceImpl implements UserService {
    private UserMapper userMapper = SessionUtil.getMapper(UserMapper.class);

    @Override
    public List<User> getUserTableWithNoCondition(Integer pageNum, Integer pageSize) {
        List<User> userTableWithNoCondition = userMapper.getUserTableWithNoCondition(pageNum, pageSize);
        return userTableWithNoCondition;
    }

    @Override
    public List<User> getUserTableWithUsername(String username) {
        return userMapper.getUserTableWithUsername(username);
    }

    @Override
    public Long getTotalWithNoCondition() {
        Long integer = userMapper.totalList();
        return integer;
    }

    @Override
    public void forbidUser(Integer userId) {
        userMapper.forbidUser(userId);
    }

    @Override
    public void resumeUser(Integer userId) {
        userMapper.resumeUser(userId);
    }

    @Override
    public Map<String, Object> userLogin(String username, String password, HttpServletResponse resp) {
        User user = userMapper.userLogin(username, password);

        Map<String, Object> result = new HashMap<>();

        if (user == null) {
            result.put("flag", 1);
            return result;
        }

        String token = JwtUtil.createToken(user);

//        try {
//            Cookie cookie1 = new Cookie("token", URLEncoder.encode(token, "utf-8"));
//            Cookie cookie2 = new Cookie("user", URLEncoder.encode(JSONUtil.toJsonStr(user), "utf-8"));
//            cookie1.setMaxAge(60 * 60 * 24 * 30); // 30天
//            cookie1.setPath("/");
//            cookie2.setMaxAge(60 * 60 * 24 * 30);
//            cookie2.setPath("/");
//            resp.addCookie(cookie1);
//            resp.addCookie(cookie2);
//            resp.setHeader("Set-Cookie", cookie1.getName() + "=" + cookie1.getValue() +
//                    "; Path=" + cookie1.getPath() + "; Max-Age=" + cookie1.getMaxAge());
//            resp.setHeader("Set-Cookie", cookie2.getName() + "=" + cookie2.getValue() +
//                    "; Path=" + cookie2.getPath() + "; Max-Age=" + cookie2.getMaxAge());
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }

        result.put("flag", 0);
        result.put("token", token);
        result.put("user", user);

        return result;
    }

    @Override
    public User getUserInfo(long userId) {
        User user = userMapper.getUserInfo(userId);
        return user;
    }

    @Override
    public int updateUserAvatar(Long userId, String avatar) {
        return userMapper.updateUserAvatar(userId, avatar);
    }

    @Override
    public int updateUserInfo(Long userId, String nickname, String description, Integer sex, Date birthday) {
        return userMapper.updateUserInfo(userId, nickname, description, sex, birthday);
    }

    @Override
    public int updateUserPassword(Long userId, String password) {
        return userMapper.updateUserPassword(userId, password);
    }

    @Override
    public int registerUser(String nickname, String username, String password) {
        /*
        * 先根据nickname用户昵称查询是否有相同昵称，昵称已存在则返回0，否则再
        根据username账号判断是否有该用户，已存在则返回1，否则生成user对象并设置
        nickname、password、username，并默认添加status=0，sex=2，birthday=new
        Date()，avatar=https://tse1-mm.cn.bing.net/th/id/OIP-C.-p5I7BvHsA1BrXI1ysXHBwAAA
        A?w=183&h=184&c=7&r=0&o=5&pid=1.7，再将该user对象保存到数据库中，并返回2
        * */
        int existCountByNickname = userMapper.isExistByNickname(nickname);
        int existCountByUsername = userMapper.isExistByUsername(username);
        if (existCountByNickname > 0) {
            return 0;
        } else if (existCountByUsername > 0) {
            return 1;
        } else {
            User user = new User();
            user.setNickname(nickname);
            user.setUsername(username);
            user.setPassword(password);
            user.setStatus(0);
            user.setSex(2);
            user.setBirthday(new Date());
            user.setAvatar("https://tse1-mm.cn.bing.net/th/id/OIP-C.-p5I7BvHsA1BrXI1ysXHBwAAAA?w=183&h=184&c=7&r=0&o=5&pid=1.7");
            int resRegisterUser = userMapper.addRegisterUser(user);
            System.out.println("RegisterInService = " + resRegisterUser);
            return 2;
        }// if else
    }

    @Override
    public List<User> getUserTableWithNickname(Integer pageNum, Integer pageSize, String nickname) {
        return userMapper.getUserTableWithNickname(pageNum, pageSize, nickname);
    }

    @Override
    public long getTotalWithNickname(String nickname) {
        return userMapper.getTotalWithNickname(nickname);
    }

    @Override
    public int updateUserCover(Long userId, String coverImage, Integer coverHeight) {
        return userMapper.updateUserCover(userId, coverImage, coverHeight);
    }

    @Override
    public int applyArtist(Long userId, String desc) {
        return userMapper.applyArtist(userId, desc);
    }

    @Override
    public List<User> getPendingArtists(Integer pageNum, Integer pageSize) {
        return userMapper.getPendingArtists(pageNum, pageSize);
    }

    @Override
    public long getPendingArtistTotal() {
        return userMapper.getPendingArtistTotal();
    }

    @Override
    public int auditArtist(Long userId, Integer status) {
        return userMapper.auditArtist(userId, status);
    }
}
