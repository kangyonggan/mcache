package com.kangyonggan.mcache.core;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * memory cache handle
 *
 * @author kangyonggan
 * @since 10/31/17
 */
public class MemoryCacheHandle implements MethodCacheHandle {

    private volatile static Map<String, CacheItem> caches = new HashMap();

    /**
     * set cache to memory
     *
     * @param prefix
     * @param key
     * @param value
     * @param expire
     * @param unit
     */
    @Override
    public void set(String prefix, String key, Object value, Long expire, MethodCache.Unit unit) {
        caches.put(key, new CacheItem(value, expire, unit));
    }

    /**
     * get cache from memory
     *
     * @param prefix
     * @param key
     * @return
     */
    @Override
    public Object get(String prefix, String key) {
        CacheItem cacheItem = caches.get(key);
        if (cacheItem == null) {
            return null;
        }
        if (cacheItem.isExpire()) {
            // remove expire cache
            caches.remove(key);
            return null;
        }

        // update cache item's  updateTime
        cacheItem.setUpdateDate(new Date());

        return cacheItem.getValue();
    }

}
