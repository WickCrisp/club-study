package com.crisp.demo.domain.approval.api;

import com.crisp.demo.domain.approval.model.ApprovalTask;
import com.crisp.demo.domain.approval.model.ApprovalType;

import java.util.List;

/**
 * 待办中心的**读取契约**
 */
public interface ApprovalQueryService {

    /** 我的待办：管理员看 scope=ADMIN，社团负责人看 scope=USER 且指派给自己 */
    List<ApprovalTask> pendingFor(Long userId, boolean isAdmin);

    /** 待办数量（角标） */
    int pendingCount(Long userId, boolean isAdmin);

    /** 我发起的申请 */
    List<ApprovalTask> myApplications(Long userId);

    ApprovalTask findById(Long id);

    /** 全系统待处理待办总数 */
    int pendingCountAll();

    /** 某个目标对象上等待处理的待办，没有返回 null */
    ApprovalTask findPendingByTarget(ApprovalType type, Long targetId);
}
