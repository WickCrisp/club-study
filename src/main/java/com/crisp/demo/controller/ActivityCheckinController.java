package com.crisp.demo.controller;

import com.crisp.demo.common.Result;
import com.crisp.demo.entity.ActivityCheckin;
import com.crisp.demo.service.ActivityCheckinService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ActivityCheckinController {

    private final ActivityCheckinService checkinService;

    @PutMapping("/api/checkin/checkin/{id}")
    @ResponseBody
    public Result<ActivityCheckin> checkin(@PathVariable Long id) {
        try {
            return Result.success("签到成功", checkinService.checkin(id));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/api/checkin/checkout/{id}")
    @ResponseBody
    public Result<ActivityCheckin> checkout(@PathVariable Long id) {
        try {
            return Result.success("签退成功", checkinService.checkout(id));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/api/checkin/activity/{activityId}")
    @ResponseBody
    public Result<List<ActivityCheckin>> listByActivity(@PathVariable Long activityId) {
        return Result.success(checkinService.findByActivityId(activityId));
    }

    @GetMapping("/api/checkin/user/{userId}")
    @ResponseBody
    public Result<List<ActivityCheckin>> listByUser(@PathVariable Long userId) {
        return Result.success(checkinService.findByUserId(userId));
    }

    @GetMapping("/api/checkin/registration/{registrationId}")
    @ResponseBody
    public Result<ActivityCheckin> getByRegistration(@PathVariable Long registrationId) {
        return Result.success(checkinService.findByRegistrationId(registrationId));
    }
}