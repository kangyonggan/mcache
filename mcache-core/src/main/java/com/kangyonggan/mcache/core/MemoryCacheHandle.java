package com.kangyonggan.mcache.core;

import java.util.HashMap;
import java.util.Map;

/**
 * @author kangyonggan
 * @since 10/31/17
 */
public class MemoryCacheHandle implements MethodCacheHandle {

    private volatile static Map<String, Object> caches = new HashMap();

    @Override
    public void set(String key, Object value) {
        System.out.println("save cache: key:" + key + ", value:" + value);
        caches.put(key, value);
    }

    @Override
    public Object get(String key) {
        Object value = caches.get(key);
        System.out.println("get cache: key:" + key + ", value:" + value);
        return value;
    }

}
