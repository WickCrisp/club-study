package com.crisp.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * 注册拦截器和参数解析器
 * 拦截所有 /api/**，公开端点在 AuthInterceptor 内部按方法+路径判断
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthInterceptor())
                .addPathPatterns("/api/**")             // 拦截所有 API
                .excludePathPatterns(                   // 仅排除无需 session 的端点
                        "/api/user/login",              // 前台登录
                        "/api/user/register",           // 注册
                        "/api/admin/login"              // 后台登录
                );
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new CurrentUserArgumentResolver());
    }
}
