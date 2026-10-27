package com.crisp.demo.domain.club.api;

import com.crisp.demo.domain.approval.api.ApprovalCommandService;
import com.crisp.demo.domain.approval.model.ApprovalTask;
import com.crisp.demo.domain.approval.model.ApprovalType;
import com.crisp.demo.domain.club.app.ClubService;
import com.crisp.demo.domain.club.model.Club;
import com.crisp.demo.domain.identity.app.UserService;
import com.crisp.demo.domain.identity.model.User;
import com.crisp.demo.shared.error.BusinessException;
import com.crisp.demo.shared.result.Result;
import com.crisp.demo.shared.security.CurrentUser;
import com.crisp.demo.shared.security.Permission;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 社团管理（负责人 / 管理员）
 *
 * 本控制器只依赖 club 域自己的能力 + identity 的查询契约，
 * 不再直接操作 club_member 表：负责人对应的成员记录由 membership 域订阅 ClubLeaderChanged 事件处理。
 */
@RestController
@RequiredArgsConstructor
public class ClubAdminController {

    private final ClubService clubService;
    private final ClubQueryService clubQueryService;
    private final UserService userService;
    private final ApprovalCommandService approvalCommandService;

    /** 我管理的社团（管理员可见全部社团） */
    @GetMapping("/api/club/my-managed")
    public Result<List<Club>> myManaged(@CurrentUser User user) {
        if (Permission.isAdmin(user)) {
            return Result.success(clubQueryService.findAll());
        }
        return Result.success(clubQueryService.findByLeaderId(user.getId()));
    }

    /** 创建社团：普通用户提交后进入待审核，管理员直接生效 */
    @PostMapping("/api/club/create")
    public Result<Club> create(@Valid @RequestBody Map<String, Object> body, @CurrentUser User user) {
        Club club = buildClub(body, null);
        if (club.getLeaderId() == null) {
            club.setLeaderId(user.getId());
        }
        applyLeaderStudentNo(body, club);

        boolean admin = Permission.isAdmin(user);
        club.setStatus(admin ? "ACTIVE" : "PENDING_REVIEW");
        Club saved = clubService.create(club);

        if (!admin) {
            approvalCommandService.createTask(ApprovalType.CLUB_CREATE, "CLUB", saved.getId(), saved.getId(),
                    user.getId(), ApprovalTask.SCOPE_ADMIN, null,
                    "社团创建申请：" + saved.getName(), "申请人：" + user.getName());
            return Result.success("申请已提交，等待管理员审批", saved);
        }
        return Result.success("创建成功", saved);
    }

    /** 更新社团 — 仅负责人或管理员；未填写负责人学号时沿用原负责人 */
    @PutMapping("/api/club/update")
    public Result<Club> update(@Valid @RequestBody Map<String, Object> body, @CurrentUser User user) {
        if (body.get("id") == null) {
            throw new BusinessException("缺少社团 ID");
        }
        Long clubId = Long.valueOf(body.get("id").toString());
        Club existing = clubQueryService.findById(clubId);
        if (existing == null) {
            throw new BusinessException("社团不存在");
        }
        Permission.requireAdminOrOwner(user, existing.getLeaderId(), "编辑社团");

        Club club = buildClub(body, existing);
        applyLeaderStudentNo(body, club);
        return Result.success("更新成功", clubService.update(club));
    }

    /** 删除社团 — 仅负责人或管理员 */
    @DeleteMapping("/api/club/{id}")
    public Result<?> delete(@PathVariable Long id, @CurrentUser User user) {
        Club existing = clubQueryService.findById(id);
        if (existing == null) {
            throw new BusinessException("社团不存在");
        }
        Permission.requireAdminOrOwner(user, existing.getLeaderId(), "删除社团");
        clubService.deleteById(id);
        return Result.success("删除成功");
    }

    // ========== 内部工具 ==========

    /**
     * 从请求体构建 Club 对象
     * existing 不为空（更新场景）时，负责人默认沿用原值，避免表单未提交负责人导致 leader_id 被清空
     */
    private Club buildClub(Map<String, Object> body, Club existing) {
        Club club = new Club();
        if (body.get("id") != null) {
            club.setId(Long.valueOf(body.get("id").toString()));
        }
        String name = asText(body.get("name"));
        if (name == null || name.isEmpty()) {
            throw new BusinessException("社团名称不能为空");
        }
        if (name.length() > 100) {
            throw new BusinessException("社团名称不超过100字");
        }
        club.setName(name);
        club.setDescription(asText(body.get("description")));
                if (body.get("categoryId") != null && !body.get("categoryId").toString().isEmpty()) {
                    club.setCategoryId(Long.valueOf(body.get("categoryId").toString()));
                }

        String status = asText(body.get("status"));
        if (status == null || status.isEmpty()) {
            status = existing != null ? existing.getStatus() : "ACTIVE";
        }
        club.setStatus(status);

        if (existing != null) {
            club.setLeaderId(existing.getLeaderId());
        }
        return club;
    }

    /** 按学号解析负责人 */
    private void applyLeaderStudentNo(Map<String, Object> body, Club club) {
        String leaderStudentNo = asText(body.get("leaderStudentNo"));
        if (leaderStudentNo == null || leaderStudentNo.isEmpty()) {
            return;
        }
        User leader = userService.findByStudentNo(leaderStudentNo);
        if (leader == null) {
            throw new BusinessException("负责人学号不存在：" + leaderStudentNo);
        }
        club.setLeaderId(leader.getId());
    }

    private String asText(Object value) {
        return value == null ? null : value.toString().trim();
    }
}
