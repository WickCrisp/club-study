package com.crisp.demo.shared.security;

import com.crisp.demo.shared.error.TooManyRequestsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 登录失败计数与限流
 *
 * 两个维度同时计数，各自独立：
 *   - 账号维度：同一账号 1 分钟内失败 5 次 → 锁定该账号的登录
 *   - IP 维度：同一 IP 1 分钟内失败 20 次 → 锁定该来源（防止换账号撞库）
 * 登录成功立即清空账号维度的计数，不影响正常用户。
 *
 * 计数放 Redis，多实例部署与重启都不会丢；
 * Redis 不可用时自动降级为单机内存计数，保证登录功能不因缓存故障而不可用。
 */
@Service
public class LoginAttemptService {

    private static final Logger log = LoggerFactory.getLogger(LoginAttemptService.class);

    private static final int ACCOUNT_LIMIT = 5;
    private static final int IP_LIMIT = 20;
    private static final Duration WINDOW = Duration.ofMinutes(1);
    private static final String PREFIX = "login:fail:";
    private static final String LOCKED_MESSAGE = "登录尝试过于频繁，请 1 分钟后再试";

    private final ObjectProvider<StringRedisTemplate> redisProvider;

    /** 降级方案：key -> [失败次数, 过期时间戳] */
    private final Map<String, long[]> memory = new ConcurrentHashMap<>();

    public LoginAttemptService(ObjectProvider<StringRedisTemplate> redisProvider) {
        this.redisProvider = redisProvider;
    }

    /** 登录前调用：已达上限直接拒绝 */
    public void checkAllowed(String account, String ip) {
        if (current(accountKey(account)) >= ACCOUNT_LIMIT) {
            throw new TooManyRequestsException(LOCKED_MESSAGE);
        }
        if (current(ipKey(ip)) >= IP_LIMIT) {
            throw new TooManyRequestsException(LOCKED_MESSAGE);
        }
    }

    /** 密码错误时调用 */
    public void onFailure(String account, String ip) {
        bump(accountKey(account));
        bump(ipKey(ip));
    }

    /** 登录成功时调用：只清账号维度，IP 维度的历史失败仍然保留 */
    public void onSuccess(String account) {
        delete(accountKey(account));
    }

    private String accountKey(String account) {
        return PREFIX + "acct:" + account;
    }

    private String ipKey(String ip) {
        return PREFIX + "ip:" + ip;
    }

    private long current(String key) {
        StringRedisTemplate redis = redisProvider.getIfAvailable();
        if (redis != null) {
            try {
                String value = redis.opsForValue().get(key);
                return value == null ? 0 : Long.parseLong(value);
            } catch (RuntimeException e) {
                log.warn("Redis 不可用，登录限流降级为内存计数：{}", e.getMessage());
            }
        }
        return memoryCurrent(key);
    }

    private void bump(String key) {
        StringRedisTemplate redis = redisProvider.getIfAvailable();
        if (redis != null) {
            try {
                Long count = redis.opsForValue().increment(key);
                if (count != null && count == 1L) {
                    redis.expire(key, WINDOW);
                }
                return;
            } catch (RuntimeException e) {
                log.warn("Redis 不可用，登录限流降级为内存计数：{}", e.getMessage());
            }
        }
        memoryBump(key);
    }

    private void delete(String key) {
        StringRedisTemplate redis = redisProvider.getIfAvailable();
        if (redis != null) {
            try {
                redis.delete(key);
            } catch (RuntimeException e) {
                log.warn("Redis 不可用，登录限流降级为内存计数：{}", e.getMessage());
            }
        }
        memory.remove(key);
    }

    private long memoryCurrent(String key) {
        long[] entry = memory.get(key);
        if (entry == null) {
            return 0;
        }
        if (entry[1] <= System.currentTimeMillis()) {
            memory.remove(key);
            return 0;
        }
        return entry[0];
    }

    private void memoryBump(String key) {
        long now = System.currentTimeMillis();
        memory.compute(key, (k, entry) -> {
            if (entry == null || entry[1] <= now) {
                return new long[]{1, now + WINDOW.toMillis()};
            }
            entry[0] = entry[0] + 1;
            return entry;
        });
    }
}
