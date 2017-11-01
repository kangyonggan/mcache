package com.kangyonggan.mcache.test;

/**
 * @author kangyonggan
 * @since 11/1/17
 */
public class User {

    public User() {

    }


    public User(Info info, Long id) {

    }



    private Long id;

    private String username;

    private String password;

    private Info info;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Info getInfo() {
        return info;
    }

    public void setInfo(Info info) {
        this.info = info;
    }
}
