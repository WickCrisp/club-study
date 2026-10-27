package com.crisp.demo.domain.approval.app;

import com.crisp.demo.domain.approval.api.ApprovalCommandService;
import com.crisp.demo.domain.approval.api.ApprovalHandler;
import com.crisp.demo.domain.approval.api.ApprovalQueryService;
import com.crisp.demo.domain.approval.infra.ApprovalTaskMapper;
import com.crisp.demo.domain.approval.model.ApprovalStatus;
import com.crisp.demo.domain.approval.model.ApprovalTask;
import com.crisp.demo.domain.approval.model.ApprovalType;
import com.crisp.demo.shared.error.BusinessException;
import com.crisp.demo.shared.error.ForbiddenException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * 待办中心实现
 *
 * 职责边界：
 * - 只负责「待办的创建、查询、决策留痕」
 * - 决策后的业务状态变更回调目标域的 {@link ApprovalHandler}，本域不碰业务表
 */
@Slf4j
@Service
public class ApprovalServiceImpl implements ApprovalCommandService, ApprovalQueryService {


    private final ApprovalTaskMapper taskMapper;
    private final Map<ApprovalType, ApprovalHandler> handlers = new EnumMap<>(ApprovalType.class);

    public ApprovalServiceImpl(ApprovalTaskMapper taskMapper, List<ApprovalHandler> handlerList) {
        this.taskMapper = taskMapper;
        for (ApprovalHandler handler : handlerList) {
            handlers.put(handler.type(), handler);
        }
        log.info("待办中心已装配 {} 个审批处理器：{}", handlers.size(), handlers.keySet());
    }

    // ===== ApprovalCommandService =====

    @Override
    @Transactional
    public void createTask(ApprovalType type, String targetType, Long targetId, Long clubId,
                           Long applicantId, String approverScope, Long approverId,
                           String title, String summary) {
        if (taskMapper.findPendingByTarget(type, targetId) != null) {
            return;   // 幂等：同一目标只保留一条待处理
        }
        ApprovalTask task = new ApprovalTask();
        task.setType(type);
        task.setTargetType(targetType);
        task.setTargetId(targetId);
        task.setClubId(clubId);
        task.setApplicantId(applicantId);
        task.setApproverScope(approverScope);
        task.setApproverId(approverId);
        task.setTitle(title);
        task.setSummary(summary);
        task.setStatus(ApprovalStatus.PENDING);
        taskMapper.insert(task);
        log.debug("创建待办 {} target={}#{}", type, targetType, targetId);
    }

    @Override
    @Transactional
    public void approve(Long taskId, Long decidedBy, String remark) {
        decide(taskId, decidedBy, remark, ApprovalStatus.APPROVED);
    }

    @Override
    @Transactional
    public void reject(Long taskId, Long decidedBy, String remark) {
        decide(taskId, decidedBy, remark, ApprovalStatus.REJECTED);
    }

    @Override
    @Transactional
    public void cancelByTarget(ApprovalType type, Long targetId, String remark) {
        taskMapper.cancelByTarget(type, targetId, remark);
    }

    // ===== ApprovalQueryService =====

    @Override
    public List<ApprovalTask> pendingFor(Long userId, boolean isAdmin) {
        return isAdmin ? taskMapper.findPendingForAdmin() : taskMapper.findPendingForApprover(userId);
    }

    @Override
    public int pendingCount(Long userId, boolean isAdmin) {
        return isAdmin ? taskMapper.countPendingForAdmin() : taskMapper.countPendingForApprover(userId);
    }

    @Override
    public int pendingCountAll() {
        return taskMapper.countPendingAll();
    }

    @Override
    public List<ApprovalTask> myApplications(Long userId) {
        return taskMapper.findByApplicant(userId);
    }

    @Override
    public ApprovalTask findById(Long id) {
        ApprovalTask task = taskMapper.findById(id);
        if (task == null) {
            throw new BusinessException("待办不存在");
        }
        return task;
    }

    @Override
    public ApprovalTask findPendingByTarget(ApprovalType type, Long targetId) {
        return taskMapper.findPendingByTarget(type, targetId);
    }

    // ===== 内部 =====

    private void decide(Long taskId, Long decidedBy, String remark, ApprovalStatus result) {
        ApprovalTask task = findById(taskId);
        if (task.getStatus() != ApprovalStatus.PENDING) {
            throw new BusinessException("该待办已处理：" + task.getStatus());
        }
        int updated = taskMapper.decide(taskId, result, decidedBy, remark);
        if (updated == 0) {
            throw new BusinessException("该待办已被其他人处理");
        }
        ApprovalHandler handler = handlers.get(task.getType());
        if (handler == null) {
            log.warn("待办类型 {} 没有对应的处理器，仅更新待办状态", task.getType());
            return;
        }
        if (result == ApprovalStatus.APPROVED) {
            handler.onApproved(task.getTargetId(), decidedBy, remark);
        } else {
            handler.onRejected(task.getTargetId(), decidedBy, remark);
        }
    }

    /** 判断当前用户是否有权处理该待办 */
    public void requireDecidable(ApprovalTask task, Long userId, boolean isAdmin) {
        if (isAdmin) {
            return;
        }
        boolean isDesignated = ApprovalTask.SCOPE_USER.equals(task.getApproverScope())
                && userId != null && userId.equals(task.getApproverId());
        if (!isDesignated) {
            throw new ForbiddenException("无权限：该待办不归你处理");
        }
    }
}
