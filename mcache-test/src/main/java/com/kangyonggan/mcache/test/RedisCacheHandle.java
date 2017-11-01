package com.kangyonggan.mcache.test;

import com.kangyonggan.mcache.core.MethodCache;
import com.kangyonggan.mcache.core.MethodCacheHandle;

import java.util.HashMap;
import java.util.Map;

/**
 * @author kangyonggan
 * @since 10/31/17
 */
public class RedisCacheHandle implements MethodCacheHandle {

    private static Map<String, Object> caches = new HashMap();

    @Override
    public void set(String key, Object value, Long expire, MethodCache.Unit unit) {
        System.out.println("redis:save cache: key:" + key + ", value:" + value);
        caches.put(key, value);
    }

    @Override
    public Object get(String key) {
        Object value = caches.get(key);
        System.out.println("redis:get cache: key:" + key + ", value:" + value);
        return value;
    }

}
