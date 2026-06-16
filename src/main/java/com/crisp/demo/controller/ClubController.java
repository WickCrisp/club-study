package com.crisp.demo.controller;

import com.crisp.demo.common.Result;
import com.crisp.demo.entity.Club;
import com.crisp.demo.entity.User;
import com.crisp.demo.mapper.ClubMapper;
import com.crisp.demo.service.ClubService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ClubController {

    private final ClubService clubService;
    private final ClubMapper clubMapper;

    @GetMapping("/member/manage")
    public String memberManagePage() {
        return "forward:/member/manage.html";
    }

    @GetMapping("/club/list")
    public String clubListPage() {
        return "forward:/club/list.html";
    }

    @GetMapping("/club/detail/{id}")
    public String clubDetailPage() {
        return "forward:/club/detail.html";
    }

    @GetMapping("/api/club/list")
    @ResponseBody
    public Result<List<Club>> list() {
        return Result.success(clubService.findAll());
    }

    @GetMapping("/api/club/search")
    @ResponseBody
    public Result<List<Club>> search(@RequestParam String keyword) {
        return Result.success(clubService.search(keyword));
    }

    @GetMapping("/api/club/my-managed")
    @ResponseBody
    public Result<List<Club>> myManaged(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return Result.error(401, "请先登录");
        }
        return Result.success(clubMapper.findByLeaderId(user.getId()));
    }

    @GetMapping("/api/club/{id}")
    @ResponseBody
    public Result<Club> getById(@PathVariable Long id) {
        try {
            return Result.success(clubService.findById(id));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/api/club/create")
    @ResponseBody
    public Result<Club> create(@RequestBody Club club) {
        try {
            return Result.success("创建成功", clubService.create(club));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/api/club/update")
    @ResponseBody
    public Result<Club> update(@RequestBody Club club) {
        try {
            return Result.success("更新成功", clubService.update(club));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/api/club/{id}")
    @ResponseBody
    public Result<?> delete(@PathVariable Long id) {
        try {
            clubService.deleteById(id);
            return Result.success("删除成功");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}