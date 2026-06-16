package com.crisp.demo.controller;

import com.crisp.demo.common.Result;
import com.crisp.demo.entity.ActivitySummary;
import com.crisp.demo.entity.User;
import com.crisp.demo.service.ActivitySummaryService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ActivitySummaryController {

    private final ActivitySummaryService summaryService;

    @GetMapping("/api/summary/list")
    @ResponseBody
    public Result<List<ActivitySummary>> list() {
        return Result.success(summaryService.findAll());
    }

    @GetMapping("/api/summary/activity/{activityId}")
    @ResponseBody
    public Result<ActivitySummary> getByActivity(@PathVariable Long activityId) {
        return Result.success(summaryService.findByActivityId(activityId));
    }

    @GetMapping("/api/summary/{id}")
    @ResponseBody
    public Result<ActivitySummary> getById(@PathVariable Long id) {
        try {
            return Result.success(summaryService.findById(id));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/api/summary/create")
    @ResponseBody
    public Result<ActivitySummary> create(@RequestBody ActivitySummary summary, HttpSession session) {
        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                return Result.error(401, "请先登录");
            }
            summary.setCreatorId(user.getId());
            return Result.success("创建成功", summaryService.create(summary));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/api/summary/update")
    @ResponseBody
    public Result<ActivitySummary> update(@RequestBody ActivitySummary summary) {
        try {
            return Result.success("更新成功", summaryService.update(summary));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/api/summary/{id}")
    @ResponseBody
    public Result<?> delete(@PathVariable Long id) {
        try {
            summaryService.deleteById(id);
            return Result.success("删除成功");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}