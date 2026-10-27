package com.crisp.demo.domain.registration.app;

import com.crisp.demo.domain.approval.api.ApprovalHandler;
import com.crisp.demo.domain.approval.model.ApprovalType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 取消报名审批处理器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RegistrationCancelApprovalHandler implements ApprovalHandler {

    private final ActivityRegistrationService registrationService;

    @Override
    public ApprovalType type() {
        return ApprovalType.CANCEL_REGISTRATION;
    }

    @Override
    public void onApproved(Long targetId, Long decidedBy, String remark) {
        registrationService.approveCancel(targetId);
        log.info("报名 {} 取消申请通过（审批人 {}）", targetId, decidedBy);
    }

    @Override
    public void onRejected(Long targetId, Long decidedBy, String remark) {
        registrationService.rejectCancel(targetId);
        log.info("报名 {} 取消申请被驳回（审批人 {}）：{}", targetId, decidedBy, remark);
    }
}
