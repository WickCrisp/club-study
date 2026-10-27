package com.crisp.demo.domain.approval.api;

import com.crisp.demo.domain.approval.model.ApprovalType;

/**
 * 待办中心的**写入契约** —— 由各业务域在发起审批时调用
 */
public interface ApprovalCommandService {

    /**
     * 创建待办（同类型同目标已有 PENDING 时不重复创建）
     *
     * @param type          待办类型
     * @param targetType    目标对象类型（CLUB / CLUB_MEMBER / ACTIVITY_REGISTRATION / ACTIVITY）
     * @param targetId      目标对象 id
     * @param clubId        所属社团（可为 null）
     * @param applicantId   申请人
     * @param approverScope ADMIN 或 USER
     * @param approverId    scope=USER 时的审批人（社团负责人）
     * @param title         标题
     * @param summary       摘要
     */
    void createTask(ApprovalType type, String targetType, Long targetId, Long clubId,
                    Long applicantId, String approverScope, Long approverId,
                    String title, String summary);

    /** 审批通过 */
    void approve(Long taskId, Long decidedBy, String remark);

    /** 审批驳回 */
    void reject(Long taskId, Long decidedBy, String remark);

    /** 目标对象作废时关闭待办 */
    void cancelByTarget(ApprovalType type, Long targetId, String remark);
}
