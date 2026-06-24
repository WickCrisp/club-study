package com.crisp.demo.controller.portal;

import com.crisp.demo.common.Result;
import com.crisp.demo.config.CurrentUser;
import com.crisp.demo.entity.ActivityRegistration;
import com.crisp.demo.entity.User;
import com.crisp.demo.service.ActivityRegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PortalRegistrationController {

    private final ActivityRegistrationService registrationService;

    // ========== 前台 API ==========

    /** 报名活动 */
    @PostMapping("/api/registration/register")
    public Result<ActivityRegistration> register(@RequestParam Long activityId, @CurrentUser User user) {
        return Result.success("报名成功", registrationService.register(activityId, user.getId()));
    }

    /** 申请取消报名 */
    @PostMapping("/api/registration/request-cancel/{id}")
    public Result<ActivityRegistration> requestCancel(@PathVariable Long id) {
        return Result.success("取消申请已提交，请等待审批", registrationService.requestCancel(id));
    }

    /** 取消报名（直接取消，保留兼容） */
    @PutMapping("/api/registration/cancel/{id}")
    public Result<ActivityRegistration> cancel(@PathVariable Long id) {
        return Result.success("已取消报名", registrationService.cancel(id));
    }

    /** 我的报名记录 */
    @GetMapping("/api/registration/my")
    public Result<List<ActivityRegistration>> myRegistrations(@CurrentUser User user) {
        return Result.success(registrationService.findByUserId(user.getId()));
    }

    /** 检查是否已报名 */
    @GetMapping("/api/registration/check")
    public Result<ActivityRegistration> check(@RequestParam Long activityId, @CurrentUser User user) {
        ActivityRegistration reg = registrationService.findByUserAndActivity(user.getId(), activityId);
        return Result.success(reg);
    }
}
