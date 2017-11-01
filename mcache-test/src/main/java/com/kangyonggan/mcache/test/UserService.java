package com.kangyonggan.mcache.test;

import com.kangyonggan.mcache.core.MethodCache;

/**
 * @author kangyonggan
 * @since 10/31/17
 */
public class UserService {


    @MethodCache("user:${user.info.realname}")
    public static User findUserByRealname(User user) {
        System.out.println(user);
        return user;
    }

    public static void main(String[] args) {
        Info info = new Info();
        info.setRealname("xxx");

        User user = new User();
        user.setInfo(info);

        findUserByRealname(user);
        findUserByRealname(user);
    }

}
