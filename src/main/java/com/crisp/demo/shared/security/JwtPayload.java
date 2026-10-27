package com.crisp.demo.shared.security;

import com.crisp.demo.domain.identity.model.User;

import java.time.Instant;

/**
 * 令牌载荷 — 拦截器解析 JWT 后得到的最小用户信息
 *
 * 只装业务真正需要的字段（id / 学号 / 姓名 / 角色），不含密码、手机号等敏感数据：
 * 令牌会落到浏览器 localStorage，能少放就少放。
 */
public record JwtPayload(Long userId, String studentNo, String name, String role, Instant expiresAt) {

    /**
     * 还原成 {@link User}，供 {@code @CurrentUser} 注入使用
     *
     * 该对象由令牌声明构造，不是数据库快照：id 与 role 在签发时确定，
     * 有效期内不会随数据库变化（改角色需重新登录）。这是无状态令牌的固有取舍，
     * 换取的是每个请求零查库。
     */
    public User toUser() {
        User user = new User();
        user.setId(userId);
        user.setStudentNo(studentNo);
        user.setName(name);
        user.setRole(role);
        return user;
    }
}
