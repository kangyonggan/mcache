package com.kangyonggan.mcache.test;

import com.kangyonggan.mcache.core.MethodCache;

/**
 * @author kangyonggan
 * @since 10/31/17
 */
public class Demo {

    @MethodCache
    public static int add(int a, int b) {
        return a + b;
    }

    public static void main(String[] args) {
        System.out.println(add(1, 2));
    }

}
