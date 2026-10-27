package com.crisp.demo.domain.showcase.api;

import com.crisp.demo.domain.club.api.ClubQueryService;
import com.crisp.demo.domain.club.model.Club;
import com.crisp.demo.domain.identity.model.User;
import com.crisp.demo.domain.showcase.app.ClubShowcaseService;
import com.crisp.demo.domain.showcase.model.ClubShowcase;
import com.crisp.demo.shared.error.ForbiddenException;
import com.crisp.demo.shared.result.Result;
import com.crisp.demo.shared.security.CurrentUser;
import com.crisp.demo.shared.security.Permission;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 社团风采 —— 浏览公开；维护限社团负责人或管理员
 */
@RestController
@RequiredArgsConstructor
public class ClubShowcaseController {

    private final ClubShowcaseService showcaseService;
    private final ClubQueryService clubQueryService;

    // ========== 公开 ==========

    /** 首页：全部已发布风采 */
    @GetMapping("/api/club-showcase/list")
    public Result<List<ClubShowcase>> list() {
        return Result.success(showcaseService.findAllPublished());
    }

    /** 社团聚合页：某社团的全部已发布风采 */
    @GetMapping("/api/club-showcase/club/{clubId}")
    public Result<List<ClubShowcase>> byClub(@PathVariable Long clubId) {
        return Result.success(showcaseService.findByClubId(clubId));
    }

    @GetMapping("/api/club-showcase/{id}")
    public Result<ClubShowcase> detail(@PathVariable Long id) {
        return Result.success(showcaseService.findById(id));
    }

    // ========== 管理（负责人 / 管理员） ==========

    /** 某社团的全部条目（含草稿） */
    @GetMapping("/api/club-showcase/admin/club/{clubId}")
    public Result<List<ClubShowcase>> manageList(@PathVariable Long clubId, @CurrentUser User user) {
        requireClubManager(user, clubId);
        return Result.success(showcaseService.findAllByClubId(clubId));
    }

    @PostMapping("/api/club-showcase")
    public Result<ClubShowcase> create(@Valid @RequestBody ClubShowcase showcase, @CurrentUser User user) {
        requireClubManager(user, showcase.getClubId());
        return Result.success("发布成功", showcaseService.create(showcase, user.getId()));
    }

    @PutMapping("/api/club-showcase/{id}")
    public Result<ClubShowcase> update(@PathVariable Long id, @RequestBody ClubShowcase showcase,
                                       @CurrentUser User user) {
        ClubShowcase exist = showcaseService.findById(id);
        requireClubManager(user, exist.getClubId());
        showcase.setId(id);
        return Result.success("更新成功", showcaseService.update(showcase));
    }

    @DeleteMapping("/api/club-showcase/{id}")
    public Result<?> delete(@PathVariable Long id, @CurrentUser User user) {
        ClubShowcase exist = showcaseService.findById(id);
        requireClubManager(user, exist.getClubId());
        showcaseService.deleteById(id);
        return Result.success("删除成功");
    }

    private void requireClubManager(User user, Long clubId) {
        if (Permission.isAdmin(user)) {
            return;
        }
        Club club = clubQueryService.findById(clubId);
        if (club == null || !user.getId().equals(club.getLeaderId())) {
            throw new ForbiddenException("无权限：仅社团负责人或管理员可维护社团风采");
        }
    }
}
