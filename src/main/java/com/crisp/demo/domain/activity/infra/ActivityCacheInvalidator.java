package com.crisp.demo.domain.activity.infra;

import com.crisp.demo.shared.cache.CacheEvictor;
import com.crisp.demo.shared.cache.CacheNames;
import com.crisp.demo.shared.event.ActivityOccupancyChanged;
import com.crisp.demo.shared.event.ClubDeleted;
import com.crisp.demo.shared.event.UserDeleted;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * activity 域的缓存失效器 — 只清自己的 activity:* 命名空间
 *
 * 订阅：
 *  - ActivityOccupancyChanged：registration 域改了报名人数 → 活动详情里的人数需要刷新
 *  - ClubDeleted：社团删除会级联删除其活动 → 清掉活动缓存
 *  - UserDeleted：用户删除会级联删除其发布的活动 → 清掉活动缓存
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ActivityCacheInvalidator {

    private final CacheEvictor evictor;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onOccupancyChanged(ActivityOccupancyChanged event) {
        log.debug("活动人数变化，清理活动缓存 activityId={}", event.activityId());
        evictor.evictAll(CacheNames.ACTIVITY_DETAIL, CacheNames.ACTIVITY_LIST);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onClubDeleted(ClubDeleted event) {
        log.debug("社团 {} 已删除，清理活动缓存", event.clubId());
        evictor.evictAll(CacheNames.ACTIVITY_DETAIL, CacheNames.ACTIVITY_LIST);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onUserDeleted(UserDeleted event) {
        log.debug("用户 {} 已删除，清理活动缓存", event.userId());
        evictor.evictAll(CacheNames.ACTIVITY_DETAIL, CacheNames.ACTIVITY_LIST);
    }
}
