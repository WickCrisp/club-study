package com.crisp.demo.domain.activity.app;

import com.crisp.demo.domain.approval.api.ApprovalHandler;
import com.crisp.demo.domain.approval.model.ApprovalType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 活动审核处理器 — 待办中心决策后回写活动状态
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ActivityReviewApprovalHandler implements ApprovalHandler {

    private final ActivityService activityService;

    @Override
    public ApprovalType type() {
        return ApprovalType.ACTIVITY_REVIEW;
    }

    @Override
    public void onApproved(Long targetId, Long decidedBy, String remark) {
        activityService.review(targetId, true);
        log.info("活动 {} 审核通过（审批人 {}）", targetId, decidedBy);
    }

    @Override
    public void onRejected(Long targetId, Long decidedBy, String remark) {
        activityService.review(targetId, false);
        log.info("活动 {} 审核驳回（审批人 {}）：{}", targetId, decidedBy, remark);
    }
}
