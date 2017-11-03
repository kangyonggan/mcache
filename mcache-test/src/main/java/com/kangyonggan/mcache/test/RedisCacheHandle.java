package com.kangyonggan.mcache.test;

import com.kangyonggan.mcache.core.MethodCache;
import com.kangyonggan.mcache.core.MethodCacheHandle;

/**
 * @author kangyonggan
 * @since 10/31/17
 */
public class RedisCacheHandle implements MethodCacheHandle {

    @Override
    public void set(String key, Object value, Long expire, MethodCache.Unit unit) {
        System.out.println("redis:save cache: key:" + key + ", value:" + value);
    }

    @Override
    public Object get(String key) {
        System.out.println("redis:get cache: key:" + key);
        return null;
    }

    @Override
    public void delete(String key) {
        System.out.println("redis:delete cache: key:" + key);
    }

}
