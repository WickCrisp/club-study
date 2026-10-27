package com.crisp.demo.domain.announcement.api;

import com.crisp.demo.domain.announcement.app.AnnouncementService;
import com.crisp.demo.domain.announcement.model.Announcement;
import com.crisp.demo.domain.identity.model.User;
import com.crisp.demo.shared.result.Result;
import com.crisp.demo.shared.security.CurrentUser;
import com.crisp.demo.shared.security.RequireRole;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 全校公告 — 浏览公开，发布/维护仅管理员
 */
@RestController
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    // ========== 公开 ==========

    @GetMapping("/api/announcement/list")
    public Result<List<Announcement>> list() {
        return Result.success(announcementService.findPublished());
    }

    @GetMapping("/api/announcement/{id}")
    public Result<Announcement> detail(@PathVariable Long id) {
        return Result.success(announcementService.findById(id));
    }

    // ========== 管理员 ==========

    @GetMapping("/api/announcement/admin/list")
    @RequireRole(value = "ADMIN", action = "查看全部公告")
    public Result<List<Announcement>> adminList() {
        return Result.success(announcementService.findAll());
    }

    @PostMapping("/api/announcement")
    @RequireRole(value = "ADMIN", action = "发布公告")
    public Result<Announcement> create(@Valid @RequestBody Announcement announcement, @CurrentUser User user) {
        return Result.success("创建成功", announcementService.create(announcement, user.getId()));
    }

    @PutMapping("/api/announcement/{id}")
    @RequireRole(value = "ADMIN", action = "修改公告")
    public Result<Announcement> update(@PathVariable Long id, @RequestBody Announcement announcement) {
        announcement.setId(id);
        return Result.success("更新成功", announcementService.update(announcement));
    }

    @PutMapping("/api/announcement/{id}/status")
    @RequireRole(value = "ADMIN", action = "变更公告状态")
    public Result<Announcement> changeStatus(@PathVariable Long id, @RequestParam String status) {
        return Result.success("状态更新成功", announcementService.changeStatus(id, status));
    }

    @DeleteMapping("/api/announcement/{id}")
    @RequireRole(value = "ADMIN", action = "删除公告")
    public Result<?> delete(@PathVariable Long id) {
        announcementService.deleteById(id);
        return Result.success("删除成功");
    }
}
