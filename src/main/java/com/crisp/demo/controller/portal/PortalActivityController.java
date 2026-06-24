package com.crisp.demo.controller.portal;

import com.crisp.demo.common.Result;
import com.crisp.demo.entity.Activity;
import com.crisp.demo.service.ActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PortalActivityController {

    private final ActivityService activityService;

    // ========== 页面路由 ==========

    @GetMapping("/activity/list")
    public String activityListPage() {
        return "forward:/activity/list.html";
    }

    @GetMapping("/activity/detail/{id}")
    public String activityDetailPage() {
        return "forward:/activity/detail.html";
    }

    // ========== 前台 API ==========

    /** 活动列表 */
    @GetMapping("/api/activity/list")
    public Result<List<Activity>> list() {
        return Result.success(activityService.findAll());
    }

    /** 按社团查活动 */
    @GetMapping("/api/activity/club/{clubId}")
    public Result<List<Activity>> listByClub(@PathVariable Long clubId) {
        return Result.success(activityService.findByClubId(clubId));
    }

    /** 按状态查活动 */
    @GetMapping("/api/activity/status/{status}")
    public Result<List<Activity>> listByStatus(@PathVariable String status) {
        return Result.success(activityService.findByStatus(status));
    }

    /** 搜索活动 */
    @GetMapping("/api/activity/search")
    public Result<List<Activity>> search(@RequestParam String keyword) {
        return Result.success(activityService.search(keyword));
    }

    /** 活动详情 */
    @GetMapping("/api/activity/{id}")
    public Result<Activity> getById(@PathVariable Long id) {
        return Result.success(activityService.findById(id));
    }
}
