package com.crisp.demo.shared.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 密码哈希：BCrypt 为主，历史 MD5 仍可校验并标记为待升级
 */
class PasswordServiceTest {

    private final PasswordService passwordService = new PasswordService(10);

    @Test
    @DisplayName("BCrypt 哈希可以校验，且每次结果不同（随机盐）")
    void bcryptEncodeAndMatch() {
        String hash = passwordService.encode("2233");

        assertTrue(hash.startsWith("$2"), "应该输出 BCrypt 格式");
        assertTrue(passwordService.matches("2233", hash));
        assertFalse(passwordService.matches("2234", hash));
        assertFalse(passwordService.needsUpgrade(hash), "BCrypt 不需要再升级");

        assertNotEquals(hash, passwordService.encode("2233"), "同一密码两次哈希应不同");
    }

    @Test
    @DisplayName("历史 MD5 密码仍能登录，并被标记为待升级")
    void legacyMd5StillWorks() {
        // 种子数据里 Yang@2026 的 MD5
        String md5 = "dfc28e98d88ac4befe4e9115fe71eb1f";

        assertTrue(passwordService.matches("Yang@2026", md5));
        assertFalse(passwordService.matches("yang@2026", md5), "MD5 区分大小写");
        assertTrue(passwordService.needsUpgrade(md5));
    }

    @Test
    @DisplayName("无法识别的格式一律拒绝，不抛异常")
    void unknownFormatRejected() {
        assertFalse(passwordService.matches("x", "not-a-hash"));
        assertFalse(passwordService.matches("x", null));
        assertFalse(passwordService.matches(null, "abc"));
        assertEquals(false, passwordService.needsUpgrade(null));
    }
}
