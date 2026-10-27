package com.crisp.demo.shared.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;

/**
 * 生产环境启动自检
 *
 * 目的：防止"配置漏了但应用照样起来"这类最危险的情况——
 * 带着开发密钥跑在生产上，表面一切正常，实际谁都能伪造管理员令牌。
 *
 * 检查项：JWT 密钥不得含开发密钥特征串、数据库口令不得短于 8 位。
 * 注意：本类不得硬编码任何真实口令（源码会进公开仓库），开发配置只存在于被 gitignore 的本地文件中。
 *
 * 只在 prod profile 下生效；检查不通过直接抛异常终止启动。
 */
@Component
public class StartupSecurityCheck {

    /** dev 配置里的密钥特征串，生产出现即视为误用（仅特征标记，非真实凭据） */
    private static final String DEV_SECRET_MARK = "dev-only-jwt-secret";

    public StartupSecurityCheck(Environment environment,
                                @Value("${app.jwt.secret}") String jwtSecret,
                                @Value("${spring.datasource.password}") String dbPassword) {
        if (!environment.acceptsProfiles(Profiles.of("prod"))) {
            return;
        }

        if (jwtSecret == null || jwtSecret.isBlank() || jwtSecret.contains(DEV_SECRET_MARK)) {
            throw new IllegalStateException(
                    "生产环境启动失败：app.jwt.secret 仍是开发密钥。请通过环境变量 JWT_SECRET 注入随机串"
                            + "（生成方式：openssl rand -base64 48）");
        }
        if (dbPassword == null || dbPassword.length() < 8) {
            throw new IllegalStateException("生产环境启动失败：数据库口令过短，至少 8 位");
        }
    }
}
