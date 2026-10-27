package com.crisp.demo.domain.membership.app;

import com.crisp.demo.domain.approval.api.ApprovalHandler;
import com.crisp.demo.domain.approval.model.ApprovalType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 退社审批处理器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MemberQuitApprovalHandler implements ApprovalHandler {

    private final ClubMemberService clubMemberService;

    @Override
    public ApprovalType type() {
        return ApprovalType.QUIT;
    }

    @Override
    public void onApproved(Long targetId, Long decidedBy, String remark) {
        clubMemberService.approveQuit(targetId);
        log.info("成员记录 {} 退社审批通过（审批人 {}）", targetId, decidedBy);
    }

    @Override
    public void onRejected(Long targetId, Long decidedBy, String remark) {
        clubMemberService.rejectQuit(targetId);
        log.info("成员记录 {} 退社申请被驳回（审批人 {}）：{}", targetId, decidedBy, remark);
    }
}
