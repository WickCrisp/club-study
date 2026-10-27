package com.crisp.demo.shared.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

/**
 * 缓存清理工具 — 供各域在收到领域事件后清理**自己的**缓存命名空间
 */
@Component
@RequiredArgsConstructor
public class CacheEvictor {

    private final CacheManager cacheManager;

    /** 清空指定命名空间下的全部条目 */
    public void evictAll(String... cacheNames) {
        for (String name : cacheNames) {
            Cache cache = cacheManager.getCache(name);
            if (cache != null) {
                cache.clear();
            }
        }
    }
}
