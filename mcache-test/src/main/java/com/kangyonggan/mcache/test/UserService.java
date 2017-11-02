package com.kangyonggan.mcache.test;

import com.kangyonggan.mcache.core.MethodCache;
import com.kangyonggan.mcache.core.MethodCacheConfig;

/**
 * @author kangyonggan
 * @since 10/31/17
 */
public class UserService {

    public static User func1(User user) {
        User u1 = new User();
        u1.setId(1L);

        return u1;
    }

    @MethodCache(value = "user:${user.info.realname}")
    public static User func2(User user) {
        if (user != null) {
            return user;
        }

        return new User();
    }

    @MethodCache(value = "user:${user.info.realname}")
    public static User findUserByRealname(User user) {
        System.out.println("没走缓存：" + user);
        if (user != null) {
            return user;
        }

        return new User();
    }

    public static User findUserByRealname2(User user) {
        User u = new User();
        u.setId(1L);

        return u;
    }

    public static void main(String[] args) throws Exception {
        // 设置全局prefix，优先使用注解中的prefix
        MethodCacheConfig.setPrefix("be");

        Info info = new Info();
        info.setRealname("xxx");

        User user = new User();
        user.setInfo(info);

        System.out.println("第1次调用：");
        System.out.println(findUserByRealname(user));
        System.out.println("第2次调用：");
        System.out.println(findUserByRealname(user));

        Thread.sleep(6 * 1000);

        System.out.println("第3次调用：");
        System.out.println(findUserByRealname(user));

    }

}
