package com.kangyonggan.mcache.core;

/**
 * @author kangyonggan
 * @since 10/31/17
 */
public interface MethodCacheHandle {

    void set(String prefix, String key, Object value, Long expire, MethodCache.Unit unit);

    Object get(String prefix, String key);

}
