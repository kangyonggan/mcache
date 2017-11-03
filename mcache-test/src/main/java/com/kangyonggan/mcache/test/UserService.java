package com.kangyonggan.mcache.test;

import com.kangyonggan.mcache.core.MethodCache;
import com.kangyonggan.mcache.core.StringUtil;
import com.kangyonggan.methodlogger.MethodLogger;

/**
 * @author kangyonggan
 * @since 10/31/17
 */
public class UserService extends Info {

    @MethodCache("user:username:${username}")
    @MethodLogger
    public User findUserByUsername(String username) {
        if (StringUtil.isEmpty(username)) {
            return null;
        }

        User user = new User();
        user.setUsername(username);
        return new User();
    }

}
