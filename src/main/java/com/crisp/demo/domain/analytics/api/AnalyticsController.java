package com.crisp.demo.domain.analytics.api;

import com.crisp.demo.domain.analytics.app.AnalyticsService;
import com.crisp.demo.domain.club.api.ClubQueryService;
import com.crisp.demo.domain.club.model.Club;
import com.crisp.demo.domain.identity.model.User;
import com.crisp.demo.shared.error.ForbiddenException;
import com.crisp.demo.shared.result.Result;
import com.crisp.demo.shared.security.CurrentUser;
import com.crisp.demo.shared.security.Permission;
import com.crisp.demo.shared.security.RequireRole;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 统计 — 管理员看全局，社团负责人看本社团
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final ClubQueryService clubQueryService;

    /** 全局统计（仅管理员）：角色在拦截器阶段判定，方法体不必再判一次 */
    @GetMapping("/global")
    @RequireRole(value = "ADMIN", action = "查看全局统计")
    public Result<Map<String, Object>> global() {
        return Result.success(analyticsService.global());
    }

    /** 社团统计（负责人或管理员）：属于数据级归属判断，留在方法内 */
    @GetMapping("/club/{clubId}")
    public Result<Map<String, Object>> club(@PathVariable Long clubId, @CurrentUser User user) {
        if (!Permission.isAdmin(user)) {
            Club club = clubQueryService.findById(clubId);
            if (club == null || !user.getId().equals(club.getLeaderId())) {
                throw new ForbiddenException("无权限：仅社团负责人或管理员可查看该社团统计");
            }
        }
        return Result.success(analyticsService.club(clubId));
    }
}
