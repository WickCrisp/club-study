package com.crisp.demo.domain.membership.api;

import com.crisp.demo.domain.approval.api.ApprovalCommandService;
import com.crisp.demo.domain.approval.model.ApprovalTask;
import com.crisp.demo.domain.approval.model.ApprovalType;
import com.crisp.demo.domain.club.api.ClubQueryService;
import com.crisp.demo.domain.club.model.Club;
import com.crisp.demo.domain.identity.model.User;
import com.crisp.demo.domain.membership.app.ClubMemberService;
import com.crisp.demo.domain.membership.model.ClubMember;
import com.crisp.demo.shared.error.BusinessException;
import com.crisp.demo.shared.error.ForbiddenException;
import com.crisp.demo.shared.result.Result;
import com.crisp.demo.shared.security.CurrentUser;
import com.crisp.demo.shared.security.Permission;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 成员（学生端）—— 申请入社 / 申请退社后进入待办中心，由社团负责人或管理员审批
 */
@RestController
@RequiredArgsConstructor
public class MemberController {

    private final ClubMemberService clubMemberService;
    private final ClubQueryService clubQueryService;
    private final ApprovalCommandService approvalCommandService;

    /** 申请加入社团 */
    @PostMapping("/api/club-member/apply")
    public Result<ClubMember> apply(@RequestParam Long clubId,
                                    @RequestParam(required = false) String applyReason,
                                    @CurrentUser User user) {
        ClubMember member = clubMemberService.apply(user.getId(), clubId, applyReason);
        createTask(ApprovalType.JOIN, clubId, member, user, "入社申请");
        return Result.success("申请已提交，等待审批", member);
    }

    /** 申请退出社团 */
    @PostMapping("/api/club-member/request-quit/{id}")
    public Result<ClubMember> requestQuit(@PathVariable Long id, @CurrentUser User user) {
        ClubMember member = clubMemberService.findById(id);
        requireSelfOrAdmin(user, member, "申请退出社团");
        ClubMember updated = clubMemberService.requestQuit(id);
        createTask(ApprovalType.QUIT, member.getClubId(), updated, user, "退社申请");
        return Result.success("退出申请已提交，请等待审批", updated);
    }

    /** 直接退出社团（无需审批） */
    @PutMapping("/api/club-member/quit/{id}")
    public Result<ClubMember> quit(@PathVariable Long id, @CurrentUser User user) {
        ClubMember member = clubMemberService.findById(id);
        requireSelfOrAdmin(user, member, "退出社团");
        return Result.success("已退出社团", clubMemberService.quit(id));
    }

    /** 我的社团 */
    @GetMapping("/api/club-member/my")
    public Result<List<ClubMember>> myMembers(@CurrentUser User user) {
        return Result.success(clubMemberService.findByUserId(user.getId()));
    }

    // ========== 内部工具 ==========

    /** 审批人默认是社团负责人；社团没有负责人时交给管理员 */
    private void createTask(ApprovalType type, Long clubId, ClubMember member, User applicant, String action) {
        Club club = clubQueryService.findById(clubId);
        if (club == null) {
            throw new BusinessException("社团不存在");
        }
        Long leaderId = club.getLeaderId();
        String scope = leaderId == null ? ApprovalTask.SCOPE_ADMIN : ApprovalTask.SCOPE_USER;
        approvalCommandService.createTask(type, "CLUB_MEMBER", member.getId(), clubId,
                applicant.getId(), scope, leaderId,
                action + "：" + club.getName(),
                "申请人：" + applicant.getName());
    }

    private void requireSelfOrAdmin(User user, ClubMember member, String action) {
        if (!user.getId().equals(member.getUserId()) && !Permission.isAdmin(user)) {
            throw new ForbiddenException("无权限：" + action + "仅成员本人或管理员可操作");
        }
    }
}
