package com.crisp.demo.domain.approval.api;

import com.crisp.demo.domain.approval.app.ApprovalServiceImpl;
import com.crisp.demo.domain.approval.model.ApprovalTask;
import com.crisp.demo.domain.identity.model.User;
import com.crisp.demo.shared.result.Result;
import com.crisp.demo.shared.security.CurrentUser;
import com.crisp.demo.shared.security.Permission;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 待办中心 — 五类审批的统一入口
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/approval")
public class ApprovalController {

    private final ApprovalServiceImpl approvalService;   // 需要 requireDecidable（内部判定方法）

    /** 我的待办 */
    @GetMapping("/pending")
    public Result<List<ApprovalTask>> pending(@CurrentUser User user) {
        return Result.success(approvalService.pendingFor(user.getId(), Permission.isAdmin(user)));
    }

    /** 待办数量（角标） */
    @GetMapping("/count")
    public Result<Integer> count(@CurrentUser User user) {
        return Result.success(approvalService.pendingCount(user.getId(), Permission.isAdmin(user)));
    }

    /** 我发起的申请 */
    @GetMapping("/my")
    public Result<List<ApprovalTask>> myApplications(@CurrentUser User user) {
        return Result.success(approvalService.myApplications(user.getId()));
    }

    /** 待办详情 */
    @GetMapping("/{id}")
    public Result<ApprovalTask> detail(@PathVariable Long id, @CurrentUser User user) {
        ApprovalTask task = approvalService.findById(id);
        approvalService.requireDecidable(task, user.getId(), Permission.isAdmin(user));
        return Result.success(task);
    }

    /** 审批通过 */
    @PutMapping("/{id}/approve")
    public Result<?> approve(@PathVariable Long id,
                             @RequestParam(required = false) String remark,
                             @CurrentUser User user) {
        ApprovalTask task = approvalService.findById(id);
        approvalService.requireDecidable(task, user.getId(), Permission.isAdmin(user));
        approvalService.approve(id, user.getId(), remark);
        return Result.success("审批通过");
    }

    /** 审批驳回 */
    @PutMapping("/{id}/reject")
    public Result<?> reject(@PathVariable Long id,
                            @RequestParam(required = false) String remark,
                            @CurrentUser User user) {
        ApprovalTask task = approvalService.findById(id);
        approvalService.requireDecidable(task, user.getId(), Permission.isAdmin(user));
        approvalService.reject(id, user.getId(), remark);
        return Result.success("已驳回");
    }
}
