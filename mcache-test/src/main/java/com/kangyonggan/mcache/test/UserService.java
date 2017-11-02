package com.kangyonggan.mcache.test;

import com.kangyonggan.mcache.core.MethodCache;

/**
 * @author kangyonggan
 * @since 10/31/17
 */
public class UserService {

    @MethodCache(value = "user:${user.info.realname}")
    public static User findUserByRealname(User user) {
        return new User();
    }

}
