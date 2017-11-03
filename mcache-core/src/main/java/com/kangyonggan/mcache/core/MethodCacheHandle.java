package com.kangyonggan.mcache.core;

/**
 * @author kangyonggan
 * @since 10/31/17
 */
public interface MethodCacheHandle {

    void set(String key, Object value, Long expire);

    Object get(String key);

    void delete(String key);

}
