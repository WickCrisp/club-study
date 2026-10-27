package com.crisp.demo.infrastructure.web;

import com.crisp.demo.shared.security.JwtService;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * 注册拦截器与参数解析器
 *
 * 拦截链顺序固定为「先认证、后授权」：
 *   1) JwtAuthInterceptor  —— 解析令牌、注入 currentUser、未登录返回 401
 *   2) RoleInterceptor     —— 按 @RequireRole 校验角色、无权限返回 403
 * 两条链都只作用于 /api/**（后端现在只提供 API，页面由 Nginx 托管前端产物）。
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final JwtService jwtService;

    public WebConfig(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new JwtAuthInterceptor(jwtService))
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/user/login",              // 前台登录
                        "/api/user/register",           // 注册
                        "/api/admin/login"              // 后台登录
                )
                .order(1);

        registry.addInterceptor(new RoleInterceptor())
                .addPathPatterns("/api/**")
                .order(2);
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new CurrentUserArgumentResolver());
    }
}
