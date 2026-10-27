package com.crisp.demo.infrastructure.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

/**
 * 跨源配置
 *
 * 白名单来自配置项 app.cors.origins（生产用环境变量 CORS_ORIGINS 注入），
 * 留空表示"只允许同源"——生产用 Nginx 同域反代时本来就不需要 CORS，
 * 这样也不会像以前那样对全世界开放。
 *
 * 认证用 JWT + Authorization 头，不依赖 Cookie，所以关闭 allowCredentials，跨站请求带不上登录态。
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private final String[] origins;

    public CorsConfig(@Value("${app.cors.origins:}") String origins) {
        this.origins = Arrays.stream(origins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        if (origins.length == 0) {
            return;   // 没配置白名单：不注册任何跨域规则
        }
        registry.addMapping("/api/**")
                .allowedOrigins(origins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false)
                .maxAge(3600);
    }
}
