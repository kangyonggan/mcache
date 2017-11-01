package com.kangyonggan.mcache.core;

/**
 * @author kangyonggan
 * @since 10/31/17
 */
public class MethodCacheConfig {

    private static String prefix = "";

    private static long expire = -1L;

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
