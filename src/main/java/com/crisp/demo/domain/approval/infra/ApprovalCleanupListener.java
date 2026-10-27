package com.crisp.demo.domain.approval.infra;

import com.crisp.demo.domain.approval.infra.ApprovalTaskMapper;
import com.crisp.demo.domain.approval.model.ApprovalType;
import com.crisp.demo.shared.event.ActivityDeleted;
import com.crisp.demo.shared.event.ClubDeleted;
import com.crisp.demo.shared.event.RegistrationCancelled;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 待办清理器 —— 目标对象被删除/作废时关闭相关待办，避免「孤儿待办」
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApprovalCleanupListener {

    private final ApprovalTaskMapper taskMapper;

    /** 社团被删除 → 关闭该社团下所有待处理待办 */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onClubDeleted(ClubDeleted event) {
        int n = taskMapper.cancelByClub(event.clubId(), "社团已删除");
        log.debug("社团 {} 已删除，关闭 {} 条待办", event.clubId(), n);
    }

    /** 活动被删除 → 关闭该活动的审核待办 */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onActivityDeleted(ActivityDeleted event) {
        int n = taskMapper.cancelByTarget(ApprovalType.ACTIVITY_REVIEW, event.activityId(), "活动已删除");
        log.debug("活动 {} 已删除，关闭 {} 条待办", event.activityId(), n);
    }

    /** 报名已取消 → 关闭该报名的取消申请待办 */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onRegistrationCancelled(RegistrationCancelled event) {
        int n = taskMapper.cancelByTarget(ApprovalType.CANCEL_REGISTRATION, event.registrationId(), "报名已取消");
        log.debug("报名 {} 已取消，关闭 {} 条待办", event.registrationId(), n);
    }
}
