package com.crisp.demo.controller;

import com.crisp.demo.common.Result;
import com.crisp.demo.entity.Activity;
import com.crisp.demo.entity.User;
import com.crisp.demo.mapper.ActivityMapper;
import com.crisp.demo.service.ActivityService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;
    private final ActivityMapper activityMapper;

    @GetMapping("/activity/manage")
    public String activityManagePage() {
        return "forward:/activity/manage.html";
    }

    @GetMapping("/activity/list")
    public String activityListPage() {
        return "forward:/activity/list.html";
    }

    @GetMapping("/activity/create")
    public String activityCreatePage() {
        return "forward:/activity/create.html";
    }

    @GetMapping("/activity/detail/{id}")
    public String activityDetailPage() {
        return "forward:/activity/detail.html";
    }

    @GetMapping("/api/activity/list")
    @ResponseBody
    public Result<List<Activity>> list() {
        return Result.success(activityService.findAll());
    }

    @GetMapping("/api/activity/club/{clubId}")
    @ResponseBody
    public Result<List<Activity>> listByClub(@PathVariable Long clubId) {
        return Result.success(activityService.findByClubId(clubId));
    }

    @GetMapping("/api/activity/status/{status}")
    @ResponseBody
    public Result<List<Activity>> listByStatus(@PathVariable String status) {
        return Result.success(activityService.findByStatus(status));
    }

    @GetMapping("/api/activity/search")
    @ResponseBody
    public Result<List<Activity>> search(@RequestParam String keyword) {
        return Result.success(activityService.search(keyword));
    }

    @GetMapping("/api/activity/my-published")
    @ResponseBody
    public Result<List<Activity>> myPublished(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return Result.error(401, "请先登录");
        }
        return Result.success(activityMapper.findByCreatorId(user.getId()));
    }

    @GetMapping("/api/activity/{id}")
    @ResponseBody
    public Result<Activity> getById(@PathVariable Long id) {
        try {
            return Result.success(activityService.findById(id));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/api/activity/create")
    @ResponseBody
    public Result<Activity> create(@RequestBody Activity activity, HttpSession session) {
        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                return Result.error(401, "请先登录");
            }
            activity.setCreatorId(user.getId());
            return Result.success("创建成功", activityService.create(activity));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/api/activity/update")
    @ResponseBody
    public Result<Activity> update(@RequestBody Activity activity) {
        try {
            return Result.success("更新成功", activityService.update(activity));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/api/activity/status/{id}")
    @ResponseBody
    public Result<?> updateStatus(@PathVariable Long id, @RequestParam String status) {
        try {
            activityService.updateStatus(id, status);
            return Result.success("状态更新成功");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/api/activity/{id}")
    @ResponseBody
    public Result<?> delete(@PathVariable Long id, HttpSession session) {
        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                return Result.error(401, "请先登录");
            }
            Activity activity = activityService.findById(id);
            if (!user.getId().equals(activity.getCreatorId()) && !"ADMIN".equals(user.getRole())) {
                return Result.error(403, "无权限删除：仅活动发布人和管理员可删除");
            }
            activityService.deleteById(id);
            return Result.success("删除成功");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}