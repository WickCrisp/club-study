package com.crisp.demo.domain.approval.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 审批任务（待办）
 *
 * 设计要点：
 * - 待办中心**不反向依赖**业务域：审批人范围由发起方在创建任务时给定（ADMIN 或具体 USER）
 * - 决策后通过 {@code ApprovalHandler} 回到目标域执行状态变更，状态所有权不被稀释
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalTask implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 审批人范围 */
    public static final String SCOPE_ADMIN = "ADMIN";
    public static final String SCOPE_USER = "USER";

    private Long id;
    private ApprovalType type;
    private String targetType;
    private Long targetId;
    private Long clubId;
    private Long applicantId;

    /** 审批人范围：ADMIN / USER */
    private String approverScope;
    private Long approverId;

    private String title;
    private String summary;
    private ApprovalStatus status;

    private Long decidedBy;
    private LocalDateTime decidedAt;
    private String remark;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ===== 联表展示字段 =====
    private String applicantName;
    private String applicantStudentNo;
}
