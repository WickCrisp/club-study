package com.crisp.demo.domain.registration.api;

import com.crisp.demo.domain.identity.model.User;
import com.crisp.demo.domain.registration.app.ActivityRegistrationService;
import com.crisp.demo.domain.registration.model.ActivityRegistration;
import com.crisp.demo.shared.result.Result;
import com.crisp.demo.shared.security.CurrentUser;
import com.crisp.demo.shared.security.Permission;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 报名管理（管理端 / 社团负责人）
 *
 * 管理员看全部；社团负责人只看自己社团的活动报名。
 * 取消报名的审批复用 {@code /api/registration/approve-cancel} 与 {@code reject-cancel}（在活动管理端）。
 */
@RestController
@RequiredArgsConstructor
public class RegistrationAdminController {

    private final ActivityRegistrationService registrationService;

    /** 报名列表，可按状态过滤（REGISTERED / CANCEL_PENDING / CANCELLED） */
    @GetMapping("/api/registration/admin/list")
    public Result<List<ActivityRegistration>> list(@RequestParam(required = false) String status,
                                                   @CurrentUser User user) {
        List<ActivityRegistration> all = registrationService.findAllDetail(status);
        if (Permission.isAdmin(user)) {
            return Result.success(all);
        }
        List<ActivityRegistration> mine = all.stream()
                .filter(r -> user.getId().equals(r.getLeaderId()))
                .toList();
        // 负责人没有任何报名时返回空列表：403 表示"你不该看"，不是"你没有数据"
        return Result.success(mine);
    }
}
