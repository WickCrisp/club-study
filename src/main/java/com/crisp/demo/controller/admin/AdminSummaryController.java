package com.crisp.demo.controller.admin;

import com.crisp.demo.common.Result;
import com.crisp.demo.entity.ActivitySummary;
import com.crisp.demo.config.CurrentUser;
import com.crisp.demo.entity.User;
import com.crisp.demo.service.ActivitySummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequiredArgsConstructor
public class AdminSummaryController {

    private final ActivitySummaryService summaryService;

    // ========== 后台 API ==========

    /** 创建活动总结 */
    @PostMapping("/api/summary/create")
    public Result<ActivitySummary> create(@Valid @RequestBody ActivitySummary summary, @CurrentUser User user) {
        summary.setCreatorId(user.getId());
        return Result.success("创建成功", summaryService.create(summary));
    }

    /** 更新活动总结 */
    @PutMapping("/api/summary/update")
    public Result<ActivitySummary> update(@Valid @RequestBody ActivitySummary summary) {
        return Result.success("更新成功", summaryService.update(summary));
    }

    /** 删除活动总结 */
    @DeleteMapping("/api/summary/{id}")
    public Result<?> delete(@PathVariable Long id) {
        summaryService.deleteById(id);
        return Result.success("删除成功");
    }
}
