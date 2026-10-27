package com.crisp.demo.domain.approval.model;

/**
 * 待办类型 — 五类审批统一收敛到待办中心
 */
public enum ApprovalType {

    /** 社团创建申请：学生提交 → 管理员审批 */
    CLUB_CREATE,
    /** 入社申请：学生提交 → 社团负责人或管理员审批 */
    JOIN,
    /** 退社申请：成员提交 → 社团负责人或管理员审批 */
    QUIT,
    /** 取消报名申请：学生提交 → 活动发布人/社团负责人/管理员审批 */
    CANCEL_REGISTRATION,
    /** 活动审核：发布人提交 → 管理员审批 */
    ACTIVITY_REVIEW
}
