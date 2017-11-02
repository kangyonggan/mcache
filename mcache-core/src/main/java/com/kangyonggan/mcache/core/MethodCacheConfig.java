package com.kangyonggan.mcache.core;

/**
 * method cache default configuration
 *
 * @author kangyonggan
 * @since 10/31/17
 */
public class MethodCacheConfig {

    /**
     * key's prefix
     */
    private static String prefix = "";

    /**
     * cache default expire time, -1 is forever
     */
    private static long expire = -1L;

    /**
     * cache default expire time unit
     */
    private static MethodCache.Unit unit = MethodCache.Unit.SECOND;

    public static String getPrefix() {
        return prefix;
    }

    public static void setPrefix(String prefix) {
        MethodCacheConfig.prefix = prefix;
    }

    public static long getExpire() {
        return expire;
    }

    public static void setExpire(long expire) {
        MethodCacheConfig.expire = expire;
    }

    public static MethodCache.Unit getUnit() {
        return unit;
    }

    public static void setUnit(MethodCache.Unit unit) {
        MethodCacheConfig.unit = unit;
    }

}
