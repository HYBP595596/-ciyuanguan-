package com.dimension.utils;


import com.dimension.entity.User;

public class UserHolderUtil {
    private static ThreadLocal<User> threadLocal = new ThreadLocal<>();

    public static void saveUser(User user) {
        threadLocal.set(user);
    }

    public static void removeUser() {
        threadLocal.remove();
    }

    public static User getUser() {
        return threadLocal.get();
    }
}
