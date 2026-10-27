package com.crisp.demo.domain.club.app;

import com.crisp.demo.domain.approval.api.ApprovalHandler;
import com.crisp.demo.domain.approval.model.ApprovalType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 社团创建审批处理器 — 待办中心决策后回写社团状态
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ClubCreationApprovalHandler implements ApprovalHandler {

    private final ClubService clubService;

    @Override
    public ApprovalType type() {
        return ApprovalType.CLUB_CREATE;
    }

    @Override
    public void onApproved(Long targetId, Long decidedBy, String remark) {
        clubService.updateStatus(targetId, "ACTIVE");
        log.info("社团 {} 创建申请已通过（审批人 {}）", targetId, decidedBy);
    }

    @Override
    public void onRejected(Long targetId, Long decidedBy, String remark) {
        clubService.updateStatus(targetId, "REJECTED");
        log.info("社团 {} 创建申请被驳回（审批人 {}）：{}", targetId, decidedBy, remark);
    }
}
