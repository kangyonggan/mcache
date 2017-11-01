package com.kangyonggan.mcache.test;

import com.kangyonggan.mcache.core.MethodCache;
import com.kangyonggan.mcache.core.MethodCacheConfig;

/**
 * @author kangyonggan
 * @since 10/31/17
 */
public class UserService {

    @MethodCache(value = "user:${user.info.realname}")
    public static User findUserByRealname(User user) {
        System.out.println("没走缓存：" + user);
        return user;
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
