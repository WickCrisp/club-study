package com.crisp.demo.controller;

import com.crisp.demo.common.Result;
import com.crisp.demo.entity.ActivityRegistration;
import com.crisp.demo.entity.User;
import com.crisp.demo.service.ActivityRegistrationService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ActivityRegistrationController {

    private final ActivityRegistrationService registrationService;

    @PostMapping("/api/registration/register")
    @ResponseBody
    public Result<ActivityRegistration> register(@RequestParam Long activityId, HttpSession session) {
        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                return Result.error(401, "请先登录");
            }
            return Result.success("报名成功", registrationService.register(activityId, user.getId()));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/api/registration/cancel/{id}")
    @ResponseBody
    public Result<ActivityRegistration> cancel(@PathVariable Long id) {
        try {
            return Result.success("已取消报名", registrationService.cancel(id));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/api/registration/activity/{activityId}")
    @ResponseBody
    public Result<List<ActivityRegistration>> listByActivity(@PathVariable Long activityId) {
        return Result.success(registrationService.findByActivityId(activityId));
    }

    @GetMapping("/api/registration/my")
    @ResponseBody
    public Result<List<ActivityRegistration>> myRegistrations(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return Result.error(401, "请先登录");
        }
        return Result.success(registrationService.findByUserId(user.getId()));
    }

    @GetMapping("/api/registration/check")
    @ResponseBody
    public Result<ActivityRegistration> check(@RequestParam Long activityId, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return Result.error(401, "请先登录");
        }
        ActivityRegistration reg = registrationService.findByUserAndActivity(user.getId(), activityId);
        return Result.success(reg);
    }
}