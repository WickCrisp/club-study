package com.crisp.demo.controller.portal;

import com.crisp.demo.common.Result;
import com.crisp.demo.entity.ActivitySummary;
import com.crisp.demo.service.ActivitySummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PortalSummaryController {

    private final ActivitySummaryService summaryService;

    // ========== 前台 API ==========

    /** 所有总结列表 */
    @GetMapping("/api/summary/list")
    public Result<List<ActivitySummary>> list() {
        return Result.success(summaryService.findAll());
    }

    /** 按活动查总结 */
    @GetMapping("/api/summary/activity/{activityId}")
    public Result<ActivitySummary> getByActivity(@PathVariable Long activityId) {
        return Result.success(summaryService.findByActivityId(activityId));
    }

    /** 总结详情 */
    @GetMapping("/api/summary/{id}")
    public Result<ActivitySummary> getById(@PathVariable Long id) {
        return Result.success(summaryService.findById(id));
    }
}
