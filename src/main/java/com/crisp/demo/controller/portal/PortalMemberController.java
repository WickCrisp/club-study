package com.crisp.demo.controller.portal;

import com.crisp.demo.common.Result;
import com.crisp.demo.config.CurrentUser;
import com.crisp.demo.entity.ClubMember;
import com.crisp.demo.entity.User;
import com.crisp.demo.service.ClubMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PortalMemberController {

    private final ClubMemberService clubMemberService;

    // ========== 前台 API ==========

    /** 申请加入社团 */
    @PostMapping("/api/club-member/apply")
    public Result<ClubMember> apply(@RequestParam Long clubId, @RequestParam(required = false) String applyReason, @CurrentUser User user) {
        return Result.success("申请已提交", clubMemberService.apply(user.getId(), clubId, applyReason));
    }

    /** 申请退出社团 */
    @PostMapping("/api/club-member/request-quit/{id}")
    public Result<ClubMember> requestQuit(@PathVariable Long id) {
        return Result.success("退出申请已提交，请等待审批", clubMemberService.requestQuit(id));
    }

    /** 我的社团 */
    @GetMapping("/api/club-member/my")
    public Result<List<ClubMember>> myMembers(@CurrentUser User user) {
        return Result.success(clubMemberService.findByUserId(user.getId()));
    }
}
