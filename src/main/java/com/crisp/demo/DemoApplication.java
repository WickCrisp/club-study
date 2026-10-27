package com.crisp.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * 认证已改为 JWT 无状态令牌，不再需要 Spring Session：
 * 登录态由前端保存在 localStorage，通过 Authorization 头传递，
 * 因此这里移除了 @EnableRedisHttpSession，Redis 现在只承担查询缓存。
 */
@SpringBootApplication
@EnableCaching
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

}
