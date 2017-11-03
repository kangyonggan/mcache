package com.kangyonggan.mcache.core;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author kangyonggan
 * @since 11/2/17
 */
public class MethodReturnHandle {

    private static Map<String, Object> instances = new HashMap();

    /**
     * process return value，put to cache
     *
     * @param handlePackage
     * @param prefix
     * @param key
     * @param value
     * @param expire
     * @param unit
     * @return
     */
    public static Object processReturn(String handlePackage, String prefix, String key, Object value, Long expire, MethodCache.Unit unit) {
        if (prefix == null) {
            prefix = MethodCacheConfig.getPrefix();
        }
        if (prefix != null && !prefix.equals("")) {
            key = prefix + key;
        }
        if (expire == null) {
            expire = MethodCacheConfig.getExpire();
        }
        if (unit == null) {
            unit = MethodCacheConfig.getUnit();
        }

        try {
            Class clazz = Class.forName(handlePackage);
            Method method = clazz.getDeclaredMethod("set", String.class, Object.class, Long.class);
            method.invoke(getInstance(clazz), key, value, expire * unit.getWeight());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return value;
    }

    /**
     * process return value，delete from cache
     *
     * @param handlePackage
     * @param prefix
     * @param key
     * @param value
     * @return
     */
    public static Object processDelReturn(String handlePackage, String prefix, String key, Object value) {
        if (prefix == null) {
            prefix = MethodCacheConfig.getPrefix();
        }
        if (prefix != null && !prefix.equals("")) {
            key = prefix + key;
        }

        try {
            Class clazz = Class.forName(handlePackage);
            Method method = clazz.getDeclaredMethod("delete", String.class);
            method.invoke(getInstance(clazz), key);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return value;
    }

    /**
     * get value from cache
     *
     * @param handlePackage
     * @param prefix
     * @param key
     * @return
     */
    public static Object get(String handlePackage, String prefix, String key) {
        if (prefix == null) {
            prefix = MethodCacheConfig.getPrefix();
        }
        if (prefix != null && !prefix.equals("")) {
            key = prefix + key;
        }

        try {
            Class clazz = Class.forName(handlePackage);
            Method method = clazz.getDeclaredMethod("get", String.class);
            return method.invoke(getInstance(clazz), key);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private static Object getInstance(Class clazz) throws Exception {
        Object object = instances.get(clazz.getName());
        if (object == null) {
            object = clazz.newInstance();
            instances.put(clazz.getName(), object);
        }

        return object;
    }

}
