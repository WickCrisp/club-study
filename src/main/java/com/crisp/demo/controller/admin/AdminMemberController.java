package com.crisp.demo.controller.admin;

import com.crisp.demo.common.BusinessException;
import com.crisp.demo.common.Result;
import com.crisp.demo.config.CurrentUser;
import com.crisp.demo.entity.Club;
import com.crisp.demo.entity.ClubMember;
import com.crisp.demo.entity.User;
import com.crisp.demo.mapper.ClubMapper;
import com.crisp.demo.service.ClubMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 成员审批 — 仅 ADMIN 或社团负责人有权限
 */
@RestController
@RequiredArgsConstructor
public class AdminMemberController {

    private final ClubMemberService clubMemberService;
    private final ClubMapper clubMapper;

    // ========== 后台 API ==========

    /** 批准入社申请 */
    @PutMapping("/api/club-member/approve/{id}")
    public Result<ClubMember> approve(@PathVariable Long id, @CurrentUser User user) {
        checkClubPermission(id, user);
        return Result.success("已通过申请", clubMemberService.approve(id));
    }

    /** 拒绝入社申请 */
    @PutMapping("/api/club-member/reject/{id}")
    public Result<ClubMember> reject(@PathVariable Long id, @CurrentUser User user) {
        checkClubPermission(id, user);
        return Result.success("已拒绝申请", clubMemberService.reject(id));
    }

    /** 社团成员列表 */
    @GetMapping("/api/club-member/club/{clubId}")
    public Result<List<ClubMember>> listByClub(@PathVariable Long clubId) {
        return Result.success(clubMemberService.findByClubId(clubId));
    }

    /** 待审批列表（入社申请 + 退出申请） */
    @GetMapping("/api/club-member/pending/{clubId}")
    public Result<List<ClubMember>> pendingList(@PathVariable Long clubId) {
        List<ClubMember> joinPending = clubMemberService.findPendingByClubId(clubId);
        List<ClubMember> quitPending = clubMemberService.findQuitPendingByClubId(clubId);
        joinPending.addAll(quitPending);
        return Result.success(joinPending);
    }

    /** 通过退出申请 */
    @PutMapping("/api/club-member/approve-quit/{id}")
    public Result<ClubMember> approveQuit(@PathVariable Long id, @CurrentUser User user) {
        checkClubPermission(id, user);
        return Result.success("已通过退出申请", clubMemberService.approveQuit(id));
    }

    /** 驳回退出申请 */
    @PutMapping("/api/club-member/reject-quit/{id}")
    public Result<ClubMember> rejectQuit(@PathVariable Long id, @CurrentUser User user) {
        checkClubPermission(id, user);
        return Result.success("已驳回退出申请", clubMemberService.rejectQuit(id));
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
    private void checkClubPermission(Long memberId, User user) {
        if ("ADMIN".equals(user.getRole())) return;               // 管理员全权限

        ClubMember member = clubMemberService.findById(memberId);
        Club club = clubMapper.findById(member.getClubId());
        if (club == null || !user.getId().equals(club.getLeaderId())) {
            throw new BusinessException("无权限：仅社团负责人或管理员可操作");
        }
    }
}
