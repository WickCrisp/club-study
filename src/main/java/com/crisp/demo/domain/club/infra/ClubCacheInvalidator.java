package com.crisp.demo.domain.club.infra;

import com.crisp.demo.shared.cache.CacheEvictor;
import com.crisp.demo.shared.cache.CacheNames;
import com.crisp.demo.shared.event.UserDeleted;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * club 域的缓存失效器 — 只清自己的 club:* 命名空间
 *
 * 订阅：
 *  - UserDeleted：删除用户会把其负责的社团 club.leader_id 置为 NULL，社团详情需要刷新
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ClubCacheInvalidator {

    private final CacheEvictor evictor;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onUserDeleted(UserDeleted event) {
        log.debug("用户 {} 已删除，清理社团缓存", event.userId());
        evictor.evictAll(CacheNames.CLUB_DETAIL, CacheNames.CLUB_LIST);
    }
}
