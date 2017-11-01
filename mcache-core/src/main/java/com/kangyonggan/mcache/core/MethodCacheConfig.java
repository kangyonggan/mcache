package com.kangyonggan.mcache.core;

/**
 * @author kangyonggan
 * @since 10/31/17
 */
public class MethodCacheConfig {

    private static String prefix = "";

    private static long expire = -1L;

    private static MethodCache.Unit unit = MethodCache.Unit.SECOND;

    private static Class<? extends MethodCacheHandle> methodCacheHandle = MemoryCacheHandle.class;

    public static String getPrefix() {
        return prefix;
    }

    public static long getExpire() {
        return expire;
    }

    public static MethodCache.Unit getUnit() {
        return unit;
    }

    public static Class<? extends MethodCacheHandle> getMethodCacheHandle() {
        return methodCacheHandle;
    }
}
