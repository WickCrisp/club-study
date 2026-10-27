package com.crisp.demo.domain.activity.api;

import com.crisp.demo.shared.error.BusinessException;
import com.crisp.demo.shared.error.ForbiddenException;
import com.crisp.demo.shared.security.Permission;
import com.crisp.demo.shared.result.Result;
import com.crisp.demo.domain.activity.model.Activity;
import com.crisp.demo.domain.approval.api.ApprovalCommandService;
import com.crisp.demo.domain.approval.api.ApprovalQueryService;
import com.crisp.demo.domain.approval.model.ApprovalTask;
import com.crisp.demo.domain.approval.model.ApprovalType;
import com.crisp.demo.domain.registration.model.ActivityRegistration;
import com.crisp.demo.domain.club.model.Club;
import com.crisp.demo.domain.identity.model.User;
import com.crisp.demo.shared.security.CurrentUser;
import com.crisp.demo.domain.club.api.ClubQueryService;
import com.crisp.demo.domain.registration.app.ActivityRegistrationService;
import com.crisp.demo.domain.activity.app.ActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class ActivityAdminController {

    private final ActivityService activityService;
    private final ActivityQueryService activityQueryService;
    private final ActivityRegistrationService registrationService;
    private final ClubQueryService clubQueryService;
    private final ApprovalCommandService approvalCommandService;
    private final ApprovalQueryService approvalQueryService;

    // ========== 后台 API ==========

    /** 我发布的活动（管理员可见全部，社团负责人可见本社团活动） */
    @GetMapping("/api/activity/my-published")
    public Result<List<Activity>> myPublished(@CurrentUser User user) {
        if (Permission.isAdmin(user)) {
            return Result.success(activityQueryService.findAll());
        }
        List<Activity> mine = activityQueryService.findByCreatorId(user.getId());
        List<Club> ledClubs = clubQueryService.findByLeaderId(user.getId());
        for (Club club : ledClubs) {
            for (Activity activity : activityQueryService.findByClubId(club.getId())) {
                boolean exists = mine.stream().anyMatch(a -> a.getId().equals(activity.getId()));
                if (!exists) {
                    mine.add(activity);
                }
            }
        }
        return Result.success(mine);
    }

    /** 创建活动 — 只能在自己负责的社团下发，管理员不受限 */
    @PostMapping("/api/activity/create")
    public Result<Activity> create(@Valid @RequestBody Activity activity, @CurrentUser User user) {
        Club club = clubQueryService.findById(activity.getClubId());
        if (club == null) {
            throw new BusinessException("所属社团不存在");
        }
        if (!Permission.isAdmin(user) && !user.getId().equals(club.getLeaderId())) {
            throw new ForbiddenException("无权限：仅该社团负责人或管理员可在本社团发布活动");
        }
        activity.setCreatorId(user.getId());
        return Result.success("创建成功", activityService.create(activity));
    }

    /** 更新活动 — 发布人 / 所属社团负责人 / 管理员 */
    @PutMapping("/api/activity/update")
    public Result<Activity> update(@Valid @RequestBody Activity activity, @CurrentUser User user) {
        Activity existing = activityService.findById(activity.getId());
        requireActivityManager(user, existing);
        return Result.success("更新成功", activityService.update(activity));
    }

    /** 按活动查报名记录 */
    @GetMapping("/api/registration/activity/{activityId}")
    public Result<List<ActivityRegistration>> listRegistrations(@PathVariable Long activityId) {
        return Result.success(registrationService.findByActivityId(activityId));
    }

    /** 提交审核 — 发布人 / 社团负责人 / 管理员；提交后进入待办中心等待管理员审核 */
    @PostMapping("/api/activity/{id}/submit")
    public Result<Activity> submitReview(@PathVariable Long id, @CurrentUser User user) {
        Activity existing = activityService.findById(id);
        requireActivityManager(user, existing);

        Activity submitted = activityService.submitReview(id);
        approvalCommandService.createTask(ApprovalType.ACTIVITY_REVIEW, "ACTIVITY", id, existing.getClubId(),
                user.getId(), ApprovalTask.SCOPE_ADMIN, null,
                "活动审核：" + existing.getTitle(),
                "发布人：" + user.getName() + "，参与范围：" + existing.getJoinScope());
        return Result.success("已提交审核，等待管理员审批", submitted);
    }

    /** 更新活动状态 — 发布人 / 所属社团负责人 / 管理员 */
    @PutMapping("/api/activity/status/{id}")
    public Result<?> updateStatus(@PathVariable Long id, @RequestParam String status,
                                  @CurrentUser User user) {
        Activity existing = activityService.findById(id);
        requireActivityManager(user, existing);
        // 「全部活动都要审核」：PENDING → ONGOING 只能由待办中心的审批处理器完成
        if ("PENDING".equals(existing.getStatus()) && "ONGOING".equals(status)) {
            throw new ForbiddenException("请通过待办中心审核，不能直接发布");
        }
        activityService.updateStatus(id, status);
        return Result.success("状态更新成功");
    }

    /** 删除活动 — 发布人 / 所属社团负责人 / 管理员 */
    @DeleteMapping("/api/activity/{id}")
    public Result<?> delete(@PathVariable Long id, @CurrentUser User user) {
        Activity existing = activityService.findById(id);
        requireActivityManager(user, existing);
        activityService.deleteById(id);
        return Result.success("删除成功");
    }

    // ========== 报名取消审批 ==========

        /** 通过取消报名申请（优先走待办中心，兼容旧入口） */
    @PutMapping("/api/registration/approve-cancel/{id}")
    public Result<ActivityRegistration> approveCancel(@PathVariable Long id, @CurrentUser User user) {
        ActivityRegistration registration = registrationService.findById(id);
        requireActivityManager(user, activityService.findById(registration.getActivityId()));
        com.crisp.demo.domain.approval.model.ApprovalTask task =
                approvalQueryService.findPendingByTarget(ApprovalType.CANCEL_REGISTRATION, id);
        if (task != null) {
            approvalCommandService.approve(task.getId(), user.getId(), "已通过取消申请");
        } else {
            registrationService.approveCancel(id);
        }
        return Result.success("已通过取消申请", registrationService.findById(id));
    }

        /** 驳回取消报名申请（优先走待办中心，兼容旧入口） */
    @PutMapping("/api/registration/reject-cancel/{id}")
    public Result<ActivityRegistration> rejectCancel(@PathVariable Long id, @CurrentUser User user) {
        ActivityRegistration registration = registrationService.findById(id);
        requireActivityManager(user, activityService.findById(registration.getActivityId()));
        com.crisp.demo.domain.approval.model.ApprovalTask task =
                approvalQueryService.findPendingByTarget(ApprovalType.CANCEL_REGISTRATION, id);
        if (task != null) {
            approvalCommandService.reject(task.getId(), user.getId(), "已驳回取消申请");
        } else {
            registrationService.rejectCancel(id);
        }
        return Result.success("已驳回取消申请", registrationService.findById(id));
    }

    // ========== 内部工具 ==========

    /** 活动管理权限：管理员 / 活动发布人 / 所属社团负责人 */
    private void requireActivityManager(User user, Activity activity) {
        if (Permission.isAdmin(user)) {
            return;
        }
        if (user.getId().equals(activity.getCreatorId())) {
            return;
        }
        Club club = clubQueryService.findById(activity.getClubId());
        if (club != null && user.getId().equals(club.getLeaderId())) {
            return;
        }
        throw new ForbiddenException("无权限：仅活动发布人、所属社团负责人或管理员可操作");
    }
}
