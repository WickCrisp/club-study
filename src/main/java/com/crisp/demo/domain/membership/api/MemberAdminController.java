package com.crisp.demo.domain.membership.api;

import com.crisp.demo.shared.error.BusinessException;
import com.crisp.demo.shared.error.ForbiddenException;
import com.crisp.demo.shared.result.Result;
import com.crisp.demo.shared.security.CurrentUser;
import com.crisp.demo.shared.security.Permission;
import com.crisp.demo.domain.club.model.Club;
import com.crisp.demo.domain.membership.model.ClubMember;
import com.crisp.demo.domain.identity.model.User;
import com.crisp.demo.domain.approval.api.ApprovalCommandService;
import com.crisp.demo.domain.approval.api.ApprovalQueryService;
import com.crisp.demo.domain.approval.model.ApprovalTask;
import com.crisp.demo.domain.approval.model.ApprovalType;
import com.crisp.demo.domain.club.api.ClubQueryService;
import com.crisp.demo.domain.membership.app.ClubMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 成员审批 — 仅 ADMIN 或社团负责人有权限
 */
@RestController
@RequiredArgsConstructor
public class MemberAdminController {

    private final ClubMemberService clubMemberService;
    private final ClubQueryService clubQueryService;
    private final ApprovalQueryService approvalQueryService;
    private final ApprovalCommandService approvalCommandService;

    // ========== 后台 API ==========

        /** 批准入社申请（优先走待办中心，兼容旧入口） */
    @PutMapping("/api/club-member/approve/{id}")
    public Result<ClubMember> approve(@PathVariable Long id, @CurrentUser User user) {
        checkClubPermission(id, user);
        if (decideByTask(ApprovalType.JOIN, id, user, true, "已通过申请")) {
            return Result.success("已通过申请", clubMemberService.findById(id));
        }
        return Result.success("已通过申请", clubMemberService.approve(id));
    }

        /** 拒绝入社申请（优先走待办中心，兼容旧入口） */
    @PutMapping("/api/club-member/reject/{id}")
    public Result<ClubMember> reject(@PathVariable Long id, @CurrentUser User user) {
        checkClubPermission(id, user);
        if (decideByTask(ApprovalType.JOIN, id, user, false, "已拒绝申请")) {
            return Result.success("已拒绝申请", clubMemberService.findById(id));
        }
        return Result.success("已拒绝申请", clubMemberService.reject(id));
    }

    /** 社团成员列表 */
    @GetMapping("/api/club-member/club/{clubId}")
    public Result<List<ClubMember>> listByClub(@PathVariable Long clubId) {
        return Result.success(clubMemberService.findByClubId(clubId));
    }

    /** 待审批列表（入社申请 + 退出申请） */
    /** 某社团的待审申请 — 仅管理员或该社团负责人可查 */
    @GetMapping("/api/club-member/pending/{clubId}")
    public Result<List<ClubMember>> pendingList(@PathVariable Long clubId, @CurrentUser User user) {
        checkClubAccess(clubId, user);
        List<ClubMember> joinPending = clubMemberService.findPendingByClubId(clubId);
        List<ClubMember> quitPending = clubMemberService.findQuitPendingByClubId(clubId);
        joinPending.addAll(quitPending);
        return Result.success(joinPending);
    }

        /** 通过退出申请（优先走待办中心） */
    @PutMapping("/api/club-member/approve-quit/{id}")
    public Result<ClubMember> approveQuit(@PathVariable Long id, @CurrentUser User user) {
        checkClubPermission(id, user);
        if (decideByTask(ApprovalType.QUIT, id, user, true, "已通过退出申请")) {
            return Result.success("已通过退出申请", clubMemberService.findById(id));
        }
        return Result.success("已通过退出申请", clubMemberService.approveQuit(id));
    }

        /** 驳回退出申请（优先走待办中心） */
    @PutMapping("/api/club-member/reject-quit/{id}")
    public Result<ClubMember> rejectQuit(@PathVariable Long id, @CurrentUser User user) {
        checkClubPermission(id, user);
        if (decideByTask(ApprovalType.QUIT, id, user, false, "已驳回退出申请")) {
            return Result.success("已驳回退出申请", clubMemberService.findById(id));
        }
        return Result.success("已驳回退出申请", clubMemberService.rejectQuit(id));
    }

    /** 若存在对应的待办则走待办中心决策，返回是否已处理 */
    private boolean decideByTask(ApprovalType type, Long targetId, User user, boolean approve, String remark) {
        ApprovalTask task = approvalQueryService.findPendingByTarget(type, targetId);
        if (task == null) {
            return false;
        }
        if (approve) {
            approvalCommandService.approve(task.getId(), user.getId(), remark);
        } else {
            approvalCommandService.reject(task.getId(), user.getId(), remark);
        }
        return true;
    }

    /** 删除成员记录 */
    @DeleteMapping("/api/club-member/{id}")
    public Result<?> delete(@PathVariable Long id, @CurrentUser User user) {
        checkClubPermission(id, user);
        clubMemberService.deleteById(id);
        return Result.success("删除成功");
    }

    /**
     * 权限校验：只有 ADMIN 或该社团的负责人可以操作成员
     */
    /** 按社团维度校验：管理员，或该社团负责人 */
    private void checkClubAccess(Long clubId, User user) {
        if (Permission.isAdmin(user)) {
            return;
        }
        Club club = clubQueryService.findById(clubId);
        if (club == null || !user.getId().equals(club.getLeaderId())) {
            throw new ForbiddenException("无权限：仅该社团负责人或管理员可查看待审申请");
        }
    }

    private void checkClubPermission(Long memberId, User user) {
        if ("ADMIN".equals(user.getRole())) return;               // 管理员全权限

        ClubMember member = clubMemberService.findById(memberId);
        Club club = clubQueryService.findById(member.getClubId());
        if (club == null || !user.getId().equals(club.getLeaderId())) {
            throw new BusinessException("无权限：仅社团负责人或管理员可操作");
        }
    }
}
