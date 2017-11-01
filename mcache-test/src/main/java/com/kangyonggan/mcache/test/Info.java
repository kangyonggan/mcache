package com.kangyonggan.mcache.test;

/**
 * @author kangyonggan
 * @since 11/1/17
 */
public class Info {

    private String realname;

    public String getRealname() {
        return realname;
    }

    public void setRealname(String realname) {
        this.realname = realname;
    }

    @Override
    public String toString() {
        return "Info{" +
                "realname='" + realname + '\'' +
                '}';
    }
}
