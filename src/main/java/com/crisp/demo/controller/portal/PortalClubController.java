package com.crisp.demo.controller.portal;

import com.crisp.demo.common.Result;
import com.crisp.demo.entity.Club;
import com.crisp.demo.service.ClubService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PortalClubController {

    private final ClubService clubService;

    // ========== 页面路由 ==========

    @GetMapping("/club/list")
    public String clubListPage() {
        return "forward:/club/list.html";
    }

    @GetMapping("/club/detail/{id}")
    public String clubDetailPage() {
        return "forward:/club/detail.html";
    }

    // ========== 前台 API ==========

    /** 社团列表 */
    @GetMapping("/api/club/list")
    public Result<List<Club>> list() {
        return Result.success(clubService.findAll());
    }

    /** 搜索社团 */
    @GetMapping("/api/club/search")
    public Result<List<Club>> search(@RequestParam String keyword) {
        return Result.success(clubService.search(keyword));
    }

    /** 社团详情 */
    @GetMapping("/api/club/{id}")
    public Result<Club> getById(@PathVariable Long id) {
        return Result.success(clubService.findById(id));
    }
}
