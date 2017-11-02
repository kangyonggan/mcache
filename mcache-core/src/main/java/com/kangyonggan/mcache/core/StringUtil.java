package com.kangyonggan.mcache.core;

/**
 * @author kangyonggan
 * @since 11/2/17
 */
public class StringUtil {

    public static final String EMPTY = "";

    public static String toVarName(String className) {
        return className.substring(0, 1).toLowerCase() + className.substring(1);
    }

    public static String toClassName(String varName) {
        return varName.substring(0, 1).toUpperCase() + varName.substring(1);
    }

    public static boolean isEmpty(String data) {
        return data == null || data.trim().length() == 0;
    }

    public static boolean isNotEmpty(String data) {
        return data != null && data.trim().length() > 0;
    }

    public static String getClassName(String packageName) {
        return packageName.substring(packageName.lastIndexOf(".") + 1);
    }

}
