package com.crisp.demo.shared.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 接口级角色要求 — 由 {@code RoleInterceptor} 在进入 Controller 之前校验
 *
 * 例：{@code @RequireRole(value = "ADMIN", action = "查看全部公告")}
 *
 * 与 {@link Permission} 的分工：
 * - 本注解解决"哪个角色能进这个接口"（粗粒度，接口级，认证之后立刻判断）
 * - {@link Permission#requireAdminOrOwner} 解决"这条数据归不归你"（细粒度，数据级）
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {

    /** 允许访问的角色，默认仅管理员 */
    String[] value() default {"ADMIN"};

    /** 动作名称，拼进 403 提示，便于定位是哪个操作被拒 */
    String action() default "";
}
