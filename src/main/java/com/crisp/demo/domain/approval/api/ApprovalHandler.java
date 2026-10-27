package com.crisp.demo.domain.approval.api;

import com.crisp.demo.domain.approval.model.ApprovalType;

/**
 * 审批结果处理器 —— 由**目标域**实现，待办中心在决策后同步回调
 *
 * 这样：
 * - 待办中心不需要知道各业务域的内部结构（只是回调一个接口）
 * - 状态变更仍然发生在拥有该状态的域里（状态所有权不被稀释）
 */
public interface ApprovalHandler {

    /** 本处理器负责的待办类型 */
    ApprovalType type();

    /** 审批通过 */
    void onApproved(Long targetId, Long decidedBy, String remark);

    /** 审批驳回 */
    void onRejected(Long targetId, Long decidedBy, String remark);
}
