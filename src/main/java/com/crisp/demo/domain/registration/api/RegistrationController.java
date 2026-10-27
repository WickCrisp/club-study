package com.crisp.demo.domain.registration.api;

import com.crisp.demo.domain.activity.api.ActivityQueryService;
import com.crisp.demo.domain.activity.model.Activity;
import com.crisp.demo.domain.approval.api.ApprovalCommandService;
import com.crisp.demo.domain.approval.model.ApprovalTask;
import com.crisp.demo.domain.approval.model.ApprovalType;
import com.crisp.demo.domain.club.api.ClubQueryService;
import com.crisp.demo.domain.identity.model.User;
import com.crisp.demo.domain.registration.app.ActivityRegistrationService;
import com.crisp.demo.domain.registration.model.ActivityRegistration;
import com.crisp.demo.shared.result.Result;
import com.crisp.demo.shared.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RegistrationController {

    private final ActivityRegistrationService registrationService;
    private final ActivityQueryService activityQueryService;
    private final ClubQueryService clubQueryService;
    private final ApprovalCommandService approvalCommandService;

    /** 报名活动 */
    @PostMapping("/api/registration/register")
    public Result<ActivityRegistration> register(@RequestParam Long activityId, @CurrentUser User user) {
        return Result.success("报名成功", registrationService.register(activityId, user.getId()));
    }

    /** 申请取消报名 — 进入待办中心，由社团负责人 / 活动发布人 / 管理员审批 */
    @PostMapping("/api/registration/request-cancel/{id}")
    public Result<ActivityRegistration> requestCancel(@PathVariable Long id, @CurrentUser User user) {
        ActivityRegistration reg = registrationService.requestCancel(id);

        Activity activity = activityQueryService.findById(reg.getActivityId());
        Long leaderId = activity == null ? null : clubQueryService.leaderIdOf(activity.getClubId());
        Long approverId = leaderId != null ? leaderId : (activity == null ? null : activity.getCreatorId());
        String scope = approverId == null ? ApprovalTask.SCOPE_ADMIN : ApprovalTask.SCOPE_USER;

        approvalCommandService.createTask(ApprovalType.CANCEL_REGISTRATION, "ACTIVITY_REGISTRATION", reg.getId(),
                activity == null ? null : activity.getClubId(), user.getId(), scope, approverId,
                "取消报名申请：" + (activity == null ? "" : activity.getTitle()),
                "申请人：" + user.getName());
        return Result.success("取消申请已提交，请等待审批", reg);
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

    /** 检查是否已报名（公开接口，未登录视为未报名） */
    @GetMapping("/api/registration/check")
    public Result<ActivityRegistration> check(@RequestParam Long activityId, @CurrentUser User user) {
        if (user == null) {
            return Result.success(null);
        }
        ActivityRegistration reg = registrationService.findByUserAndActivity(user.getId(), activityId);
        return Result.success(reg);
    }
}
