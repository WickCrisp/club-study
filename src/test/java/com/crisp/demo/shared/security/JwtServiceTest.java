package com.crisp.demo.shared.security;

import com.crisp.demo.domain.identity.model.User;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * JWT 签发与校验的边界测试
 *
 * 覆盖的是"令牌能不能被伪造/能不能过期还继续用"这类安全问题，
 * 而不是简单的签发-解析往返。
 */
class JwtServiceTest {

    private static final String SECRET = "unit-test-secret-key-at-least-32-bytes-long-2026";
    private static final String OTHER_SECRET = "another-unit-test-secret-key-32-bytes-2026!!";
    private static final Instant BASE = Instant.parse("2026-01-01T00:00:00Z");

    private static User sampleUser() {
        User user = new User();
        user.setId(7L);
        user.setStudentNo("202335710340");
        user.setName("杨铭");
        user.setRole("ADMIN");
        return user;
    }

    private static JwtService serviceAt(Instant instant, String secret) {
        return new JwtService(secret, Duration.ofHours(2), Clock.fixed(instant, ZoneOffset.UTC));
    }

    @Test
    @DisplayName("签发后可解析出用户身份")
    void issueThenParse() {
        JwtService jwt = serviceAt(BASE, SECRET);
        String token = jwt.issue(sampleUser());

        JwtPayload payload = jwt.parse(token);

        assertEquals(7L, payload.userId().longValue());
        assertEquals("202335710340", payload.studentNo());
        assertEquals("杨铭", payload.name());
        assertEquals("ADMIN", payload.role());
        assertNotNull(payload.expiresAt());
        assertEquals(BASE.plus(Duration.ofHours(2)), payload.expiresAt());
    }

    @Test
    @DisplayName("解析出的用户可直接注入 @CurrentUser，且不带密码")
    void payloadConvertsToUser() {
        JwtService jwt = serviceAt(BASE, SECRET);
        User user = jwt.parse(jwt.issue(sampleUser())).toUser();

        assertEquals(7L, user.getId().longValue());
        assertEquals("ADMIN", user.getRole());
        assertNull(user.getPassword());
    }

    @Test
    @DisplayName("超过有效期后解析抛出 ExpiredJwtException")
    void expiredTokenRejected() {
        String token = serviceAt(BASE, SECRET).issue(sampleUser());

        JwtService afterThreeHours = serviceAt(BASE.plus(Duration.ofHours(3)), SECRET);

        assertThrows(ExpiredJwtException.class, () -> afterThreeHours.parse(token));
    }

    @Test
    @DisplayName("换一个密钥签发的令牌无法通过校验")
    void tokenSignedByAnotherKeyRejected() {
        String token = serviceAt(BASE, OTHER_SECRET).issue(sampleUser());

        assertThrows(JwtException.class, () -> serviceAt(BASE, SECRET).parse(token));
    }

    @Test
    @DisplayName("篡改载荷会导致签名校验失败")
    void tamperedPayloadRejected() {
        String token = serviceAt(BASE, SECRET).issue(sampleUser());
        String[] parts = token.split("\\.");
        // 把载荷里的角色改成 ADMIN 的 base64 变体，签名不变
        String forgedPayload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(("{\"sub\":\"7\",\"role\":\"ADMIN\",\"exp\":"
                        + BASE.plus(Duration.ofHours(2)).getEpochSecond() + "}").getBytes(StandardCharsets.UTF_8));
        String forged = parts[0] + "." + forgedPayload + "." + parts[2];

        assertThrows(JwtException.class, () -> serviceAt(BASE, SECRET).parse(forged));
    }

    @Test
    @DisplayName("alg=none 的无签名令牌不被接受")
    void unsecuredTokenRejected() {
        Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
        String header = encoder.encodeToString(
                "{\"alg\":\"none\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));
        String payload = encoder.encodeToString(
                ("{\"sub\":\"7\",\"role\":\"ADMIN\",\"exp\":"
                        + BASE.plus(Duration.ofHours(2)).getEpochSecond() + "}").getBytes(StandardCharsets.UTF_8));
        String unsecured = header + "." + payload + ".";

        assertThrows(JwtException.class, () -> serviceAt(BASE, SECRET).parse(unsecured));
    }

    @Test
    @DisplayName("密钥不足 32 字节时启动即失败，避免弱密钥上线")
    void shortSecretRejected() {
        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> new JwtService("too-short", Duration.ofHours(2), Clock.fixed(BASE, ZoneOffset.UTC)));
        assertTrue(e.getMessage().contains("32"));
    }
}
