package com.crisp.demo.shared.security;

import com.crisp.demo.domain.identity.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/**
 * JWT 签发与校验 — HS256 对称签名
 *
 * 设计要点：
 * 1) 密钥从配置读取且强制 ≥32 字节：HS256 的密钥短于 256 位时 jjwt 会直接拒绝，
 *    这里提前在启动期抛错，避免"能启动但一登录就 500"。
 * 2) 校验时指定 {@code verifyWith}，只接受同一密钥签出的令牌；算法由密钥类型决定，
 *    因此 alg=none 之类的伪造令牌无法通过（jjwt 不会用无签名方式解析已指定密钥的令牌）。
 * 3) 时钟可注入，便于测试"已过期"这类与时间相关的分支。
 */
@Component
public class JwtService {

    public static final String CLAIM_STUDENT_NO = "studentNo";
    public static final String CLAIM_NAME = "name";
    public static final String CLAIM_ROLE = "role";

    private static final int MIN_SECRET_BYTES = 32;

    private final SecretKey key;
    private final Duration ttl;
    private final Clock clock;

    /**
     * 显式标注 @Autowired：本类有两个构造器（另一个供测试注入固定时钟），
     * 不标注时 Spring 无法判断用哪个，会去找无参构造并报 "No default constructor found"。
     */
    @Autowired
    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expire-minutes:120}") long expireMinutes) {
        this(secret, Duration.ofMinutes(expireMinutes), Clock.systemUTC());
    }

    /** 供单元测试注入固定时钟 */
    JwtService(String secret, Duration ttl, Clock clock) {
        byte[] keyBytes = secret == null ? new byte[0] : secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < MIN_SECRET_BYTES) {
            throw new IllegalStateException(
                    "app.jwt.secret 至少需要 " + MIN_SECRET_BYTES + " 字节（HS256 要求），当前 "
                            + keyBytes.length + " 字节");
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.ttl = ttl;
        this.clock = clock;
    }

    /** 签发令牌：sub 存用户 id，另附学号/姓名/角色供免查库还原用户 */
    public String issue(User user) {
        Instant now = clock.instant();
        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim(CLAIM_STUDENT_NO, user.getStudentNo())
                .claim(CLAIM_NAME, user.getName())
                .claim(CLAIM_ROLE, user.getRole())
                .id(UUID.randomUUID().toString())          // jti：为将来做黑名单/审计留的钩子
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(ttl)))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * 解析并校验令牌
     *
     * @throws io.jsonwebtoken.ExpiredJwtException 已过期
     * @throws JwtException                        签名不匹配、格式非法等
     */
    public JwtPayload parse(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .clock(() -> Date.from(clock.instant()))
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return new JwtPayload(
                Long.valueOf(claims.getSubject()),
                claims.get(CLAIM_STUDENT_NO, String.class),
                claims.get(CLAIM_NAME, String.class),
                claims.get(CLAIM_ROLE, String.class),
                claims.getExpiration().toInstant());
    }

    /** 供登录响应返回的倒计时（秒） */
    public long getExpireSeconds() {
        return ttl.toSeconds();
    }
}
