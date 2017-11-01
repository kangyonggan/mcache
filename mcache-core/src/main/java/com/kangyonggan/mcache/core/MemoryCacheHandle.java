package com.kangyonggan.mcache.core;

import java.util.HashMap;
import java.util.Map;

/**
 * @author kangyonggan
 * @since 10/31/17
 */
public class MemoryCacheHandle implements MethodCacheHandle {

    private volatile static Map<String, CacheItem> caches = new HashMap();

    @Override
    public void set(String prefix, String key, Object value, Long expire, MethodCache.Unit unit) {
        if (prefix == null) {
            prefix = MethodCacheConfig.getPrefix();
        }
        if (prefix != null && !prefix.equals("")) {
            key = prefix + ":" + key;
        }
        if (expire == null) {
            expire = MethodCacheConfig.getExpire();
        }
        if (unit == null) {
            unit = MethodCacheConfig.getUnit();
        }

        caches.put(key, new CacheItem(value, expire, unit));
    }

    @Override
    public Object get(String prefix, String key) {
        if (prefix == null) {
            prefix = MethodCacheConfig.getPrefix();
        }
        if (prefix != null && !prefix.equals("")) {
            key = prefix + ":" + key;
        }

        CacheItem cacheItem = caches.get(key);
        if (cacheItem == null) {
            return null;
        }
        if (cacheItem.isExpire()) {
            // remove expire cache
            caches.remove(key);
            return null;
        }

        return cacheItem.getValue();
    }

}
