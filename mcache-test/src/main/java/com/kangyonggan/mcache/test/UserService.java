package com.kangyonggan.mcache.test;

import com.kangyonggan.mcache.core.MethodCache;

/**
 * @author kangyonggan
 * @since 10/31/17
 */
public class UserService {


    @MethodCache("user:${user.info.realname}")
    public User findUserByRealname(User user) {
        System.out.println(user);
        return null;
    }

}
