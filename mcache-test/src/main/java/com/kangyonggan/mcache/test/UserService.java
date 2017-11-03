package com.kangyonggan.mcache.test;

import com.kangyonggan.mcache.core.MethodCache;
import com.kangyonggan.mcache.core.MethodCacheConfig;

/**
 * @author kangyonggan
 * @since 10/31/17
 */
public class UserService extends Info {

    @MethodCache(value = "user:username:${username}", expire = 3, handle = RedisCacheHandle.class)
    public static User findUserByUsername(String username) {
        System.out.println("没走缓存");
        return new User();
    }

    public static void main(String[] args) throws Exception {

        MethodCacheConfig.setUnit(MethodCache.Unit.DAY);

        findUserByUsername("admin");
        findUserByUsername("admin");

        Thread.sleep(4000);

        System.out.println("4秒后，缓存失效");
        findUserByUsername("admin");
    }

}
