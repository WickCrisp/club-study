package com.crisp.demo.controller.admin;

import com.crisp.demo.common.Result;
import com.crisp.demo.entity.Activity;
import com.crisp.demo.entity.ActivityRegistration;
import com.crisp.demo.entity.User;
import com.crisp.demo.config.CurrentUser;
import com.crisp.demo.mapper.ActivityMapper;
import com.crisp.demo.service.ActivityRegistrationService;
import com.crisp.demo.service.ActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class AdminActivityController {

    private final ActivityService activityService;
    private final ActivityMapper activityMapper;
    private final ActivityRegistrationService registrationService;

    // ========== 页面路由 ==========

    @GetMapping("/activity/manage")
    public String activityManagePage() {
        return "forward:/activity/manage.html";
    }

    @GetMapping("/activity/create")
    public String activityCreatePage() {
        return "forward:/activity/create.html";
    }

    // ========== 后台 API ==========

    /** 我发布的活动（管理员可见全部） */
    @GetMapping("/api/activity/my-published")
    public Result<List<Activity>> myPublished(@CurrentUser User user) {
        if ("ADMIN".equals(user.getRole())) {
            return Result.success(activityMapper.findAll());
        }
        return Result.success(activityMapper.findByCreatorId(user.getId()));
    }

    /** 创建活动 */
    @PostMapping("/api/activity/create")
    public Result<Activity> create(@Valid @RequestBody Activity activity, @CurrentUser User user) {
        activity.setCreatorId(user.getId());
        return Result.success("创建成功", activityService.create(activity));
    }

    /** 更新活动 */
    @PutMapping("/api/activity/update")
    public Result<Activity> update(@Valid @RequestBody Activity activity) {
        return Result.success("更新成功", activityService.update(activity));
    }

    /** 按活动查报名记录 */
    @GetMapping("/api/registration/activity/{activityId}")
    public Result<List<ActivityRegistration>> listRegistrations(@PathVariable Long activityId) {
        return Result.success(registrationService.findByActivityId(activityId));
    }

    /** 更新活动状态 */
    @PutMapping("/api/activity/status/{id}")
    public Result<?> updateStatus(@PathVariable Long id, @RequestParam String status) {
        activityService.updateStatus(id, status);
        return Result.success("状态更新成功");
    }

    /** 删除活动（仅发布人或管理员） */
    @DeleteMapping("/api/activity/{id}")
    public Result<?> delete(@PathVariable Long id, @CurrentUser User user) {
        Activity activity = activityService.findById(id);
        if (!user.getId().equals(activity.getCreatorId()) && !"ADMIN".equals(user.getRole())) {
            return Result.error(403, "无权限删除：仅活动发布人和管理员可删除");
        }
        activityService.deleteById(id);
        return Result.success("删除成功");
    }
}
