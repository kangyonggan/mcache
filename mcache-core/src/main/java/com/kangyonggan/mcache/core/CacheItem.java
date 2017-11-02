package com.kangyonggan.mcache.core;

import java.util.Date;

/**
 * Cache entity
 *
 * @author kangyonggan
 * @since 11/1/17
 */
public class CacheItem {

    /**
     * Cache value
     */
    private Object value;

    /**
     * Cache expire time
     */
    private Long expire;

    /**
     * Cache expire time's unit
     */
    private MethodCache.Unit unit;

    /**
     * Cache update time
     */
    private Date updateDate;

    public CacheItem() {
    }

    public CacheItem(Object value, Long expire, MethodCache.Unit unit) {
        this.value = value;
        this.expire = expire;
        this.unit = unit;
        this.updateDate = new Date();
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Long getExpire() {
        return expire;
    }

    public void setExpire(Long expire) {
        this.expire = expire;
    }

    public MethodCache.Unit getUnit() {
        return unit;
    }

    public void setUnit(MethodCache.Unit unit) {
        this.unit = unit;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    @Override
    public String toString() {
        return "CacheItem{" +
                "value=" + value +
                ", expire=" + expire +
                ", unit=" + unit +
                ", updateDate=" + updateDate +
                '}';
    }

    /**
     * @return
     */
    public boolean isExpire() {
        if (expire == -1) {
            return false;
        }

        if (new Date().getTime() < updateDate.getTime() + expire * unit.getWeight() * 1000) {
            return false;
        }

        return true;
    }
}
