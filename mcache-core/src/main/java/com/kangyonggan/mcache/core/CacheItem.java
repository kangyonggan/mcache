package com.kangyonggan.mcache.core;

import java.util.Date;

/**
 * @author kangyonggan
 * @since 11/1/17
 */
public class CacheItem {

    private Object value;

    private Long expire;

    private MethodCache.Unit unit;

    private Date startDate;

    public CacheItem() {
    }

    public CacheItem(Object value, Long expire, MethodCache.Unit unit) {
        this.value = value;
        this.expire = expire;
        this.unit = unit;
        this.startDate = new Date();
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

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    @Override
    public String toString() {
        return "CacheItem{" +
                "value=" + value +
                ", expire=" + expire +
                ", unit=" + unit +
                ", startDate=" + startDate +
                '}';
    }

    /**
     * @return
     */
    public boolean isExpire() {
        if (expire == -1) {
            return false;
        }

        if (new Date().getTime() < startDate.getTime() + expire * unit.getWeight() * 1000) {
            return false;
        }

        return true;
    }
}
