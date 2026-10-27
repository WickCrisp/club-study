package com.crisp.demo.domain.club.api;

import com.crisp.demo.shared.result.Result;
import com.crisp.demo.domain.club.model.Club;
import com.crisp.demo.domain.club.app.ClubService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ClubController {

    private final ClubService clubService;

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
