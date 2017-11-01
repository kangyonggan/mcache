package com.kangyonggan.mcache.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * method cache get or save
 *
 * @author kangyonggan
 * @since 10/31/17
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface MethodCache {

    /**
     * cache key
     *
     * @return
     */
    String value();

    /**
     * @return
     */
    String prefix() default "";

    /**
     * expire time， default is no expire。
     *
     * @return
     */
    long expire() default -1L;

    /**
     * expire time unit, default is second
     *
     * @return
     */
    Unit unit() default Unit.SECOND;

    /**
     * cache channel, default is memory cache
     *
     * @return
     */
    Class<? extends MethodCacheHandle> handle() default MethodCacheHandle.class;

    /**
     * expire time unit
     */
    enum Unit {
        /**
         * second
         */
        SECOND("SECOND", 1L),

        /**
         * minute
         */
        MINUTE("MINUTE", 60L),

        /**
         * hour
         */
        HOUR("HOUR", 3600L),

        /**
         * day
         */
        DAY("DAY", 216000L);

        private String name;
        private Long weight;

        Unit(String name, Long weight) {
            this.name = name;
            this.weight = weight;
        }

        public String getName() {
            return name;
        }

        public Long getWeight() {
            return weight;
        }
    }

}
