package com.dimension.utils;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Mapper 获取工具。
 *
 * 关键修复：以前 getSession() 打开的 SqlSession 从不 close，每次 mapper 调用都会
 * 泄漏一个数据库连接，最终把 MySQL 的 max_connections 占满（报 Too many connections）。
 * 现在改成动态代理：每次 mapper 方法调用都用一个临时 session，调用完立刻关闭，
 * 业务代码无需改动即可根治泄漏。
 */
public class SessionUtil {

    private static final SqlSessionFactory FACTORY;

    static {
        try {
            InputStream is = Resources.getResourceAsStream("mybatis-config.xml");
            FACTORY = new SqlSessionFactoryBuilder().build(is);
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T getMapper(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(
                clazz.getClassLoader(),
                new Class<?>[]{clazz},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        // Object 自带方法不查库
                        if (method.getDeclaringClass() == Object.class) {
                            switch (method.getName()) {
                                case "toString":
                                    return "mapper:" + clazz.getSimpleName();
                                case "hashCode":
                                    return System.identityHashCode(proxy);
                                case "equals":
                                    return proxy == (args == null ? null : args[0]);
                                default:
                                    return null;
                            }
                        }
                        // 一次调用 = 一个 session，用完即关（try-with-resources）
                        try (SqlSession session = FACTORY.openSession(true)) {
                            Object target = session.getMapper(clazz);
                            try {
                                return method.invoke(target, args);
                            } catch (InvocationTargetException e) {
                                throw e.getTargetException();
                            }
                        }
                    }
                });
    }
}
