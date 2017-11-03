package com.kangyonggan.mcache.test;

import com.kangyonggan.mcache.core.MethodCache;
import com.kangyonggan.mcache.core.MethodCacheDel;

/**
 * @author kangyonggan
 * @since 10/31/17
 */
public class UserService {

    @MethodCache(value = "demo:${user.info.realname}")
    public static User findUserByRealname(User user) {
        return new User();
    }

    public UserService() {
    }

    @Override
    public String toString() {
        return super.toString();
    }

    public String toString2() {
        return super.toString();
    }

    @MethodCache(value = "demo:${id}")
    public static Long findUserByRealname2(Long id) {
        return id;
    }

    @MethodCache(value = "demo:${user.username}")
    public static void findUserByRealname2(User user, int age) {
    }

    @MethodCacheDel(value = "demo:${user.id}")
    public void updateUser(User user) {

    }

    @MethodCacheDel(value = "demo:${user.username}")
    public int updateUser2(User user) {
        return 0;
    }

}
