package com.kangyonggan.mcache.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * method cache delete
 *
 * @author kangyonggan
 * @since 10/31/17
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface MethodCacheDel {

    /**
     * cache key
     *
     * @return
     */
    String value();

    /**
     * cache channel, default is memory cache
     *
     * @return
     */
    Class<? extends MethodCacheHandle> handle() default MethodCacheHandle.class;

}
