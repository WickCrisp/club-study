package com.crisp.demo.controller;

import com.crisp.demo.common.Result;
import com.crisp.demo.entity.ClubMember;
import com.crisp.demo.entity.User;
import com.crisp.demo.service.ClubMemberService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ClubMemberController {

    private final ClubMemberService clubMemberService;

    @PostMapping("/api/club-member/apply")
    @ResponseBody
    public Result<ClubMember> apply(@RequestParam Long clubId, @RequestParam(required = false) String applyReason, HttpSession session) {
        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                return Result.error(401, "请先登录");
            }
            return Result.success("申请已提交", clubMemberService.apply(user.getId(), clubId, applyReason));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/api/club-member/approve/{id}")
    @ResponseBody
    public Result<ClubMember> approve(@PathVariable Long id) {
        try {
            return Result.success("已通过申请", clubMemberService.approve(id));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/api/club-member/reject/{id}")
    @ResponseBody
    public Result<ClubMember> reject(@PathVariable Long id) {
        try {
            return Result.success("已拒绝申请", clubMemberService.reject(id));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/api/club-member/quit/{id}")
    @ResponseBody
    public Result<ClubMember> quit(@PathVariable Long id) {
        try {
            return Result.success("已退出社团", clubMemberService.quit(id));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/api/club-member/club/{clubId}")
    @ResponseBody
    public Result<List<ClubMember>> listByClub(@PathVariable Long clubId) {
        return Result.success(clubMemberService.findByClubId(clubId));
    }

    @GetMapping("/api/club-member/my")
    @ResponseBody
    public Result<List<ClubMember>> myMembers(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return Result.error(401, "请先登录");
        }
        return Result.success(clubMemberService.findByUserId(user.getId()));
    }

    @GetMapping("/api/club-member/pending/{clubId}")
    @ResponseBody
    public Result<List<ClubMember>> pendingList(@PathVariable Long clubId) {
        return Result.success(clubMemberService.findPendingByClubId(clubId));
    }

    @DeleteMapping("/api/club-member/{id}")
    @ResponseBody
    public Result<?> delete(@PathVariable Long id) {
        try {
            clubMemberService.deleteById(id);
            return Result.success("删除成功");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}