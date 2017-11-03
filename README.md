# Method Cache

## What?
Use compile annotation manager method return value.

## Quick Start

### Dependency

```
<dependency>
    <groupId>com.kangyonggan</groupId>
    <artifactId>mcache-core</artifactId>
    <version>1.0</version>
</dependency>
```

### Code
```
package com.kangyonggan.mcache.test;

import com.kangyonggan.mcache.core.MethodCache;

/**
 * @author kangyonggan
 * @since 10/31/17
 */
public class UserService extends Info {

    @MethodCache(value = "user:username:${username}", expire = 3)
    public static User findUserByUsername(String username) {
        System.out.println("没走缓存");
        return new User();
    }

    public static void main(String[] args) throws Exception {
        findUserByUsername("admin");
        findUserByUsername("admin");

        Thread.sleep(4000);
        
        System.out.println("4秒后，缓存失效");
        findUserByUsername("admin");
    }

}
```

Output:

```
没走缓存
4秒后，缓存失效
没走缓存
``` 

### More Usage
```
@MethodCache(value = "user:username:${username}", handle = RedisCacheHandle.class)
```

RedisCacheHandle.java

```
package com.kangyonggan.mcache.test;

import com.kangyonggan.mcache.core.MethodCacheHandle;

/**
 * @author kangyonggan
 * @since 10/31/17
 */
public class RedisCacheHandle implements MethodCacheHandle {

    @Override
    public void set(String key, Object value, Long expire) {
        System.out.println("redis:save cache: key:" + key + ", value:" + value);
    }

    @Override
    public Object get(String key) {
        System.out.println("redis:get cache: key:" + key);
        return null;
    }

    @Override
    public void delete(String key) {
        System.out.println("redis:delete cache: key:" + key);
    }

}
```

### Global Configuration
```
MethodCacheConfig.setPrefix("be");
MethodCacheConfig.setExpire(5);
MethodCacheConfig.setUnit(MethodCache.Unit.DAY);
```