package com.crisp.demo.shared.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

/**
 * 密码哈希与校验
 *
 * 新密码一律用 BCrypt（自带随机盐、可调计算强度），不再使用 MD5。
 * 但历史数据里的 MD5 还要能登录——所以校验时按库里的格式分支：
 *   - $2a$/$2b$/$2y$ 开头 → BCrypt 校验
 *   - 32 位十六进制       → 按 MD5 校验（仅用于迁移期）
 *
 * 调用方在登录成功后判断 {@link #needsUpgrade}，命中就顺手把旧哈希换成 BCrypt，
 * 用户无感，也不需要批量重置密码。
 */
@Component
public class PasswordService {

    private static final Logger log = LoggerFactory.getLogger(PasswordService.class);

    private static final Pattern BCRYPT_PATTERN = Pattern.compile("^\\$2[aby]\\$\\d{2}\\$.{53}$");
    private static final Pattern MD5_PATTERN = Pattern.compile("^[0-9a-fA-F]{32}$");

    private final BCryptPasswordEncoder encoder;

    public PasswordService(@Value("${app.password.bcrypt-strength:10}") int strength) {
        // 计算强度越高越慢；10 在一般服务器上约 60-80ms，兼顾安全与登录体验
        this.encoder = new BCryptPasswordEncoder(Math.max(10, Math.min(strength, 14)));
    }

    /** 生成 BCrypt 哈希（每次结果不同，因为盐是随机的） */
    public String encode(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    /** 校验密码，兼容历史 MD5 */
    public boolean matches(String rawPassword, String storedPassword) {
        if (rawPassword == null || storedPassword == null) {
            return false;
        }
        if (isBcrypt(storedPassword)) {
            return encoder.matches(rawPassword, storedPassword);
        }
        if (isMd5(storedPassword)) {
            String md5 = DigestUtils.md5DigestAsHex(rawPassword.getBytes(StandardCharsets.UTF_8));
            return md5.equalsIgnoreCase(storedPassword);
        }
        log.warn("无法识别的密码哈希格式，长度={}", storedPassword.length());
        return false;
    }

    /** 是否需要升级为 BCrypt（库里的还是 MD5） */
    public boolean needsUpgrade(String storedPassword) {
        return storedPassword != null && isMd5(storedPassword);
    }

    private boolean isBcrypt(String stored) {
        return BCRYPT_PATTERN.matcher(stored).matches();
    }

    private boolean isMd5(String stored) {
        return MD5_PATTERN.matcher(stored).matches();
    }
}
