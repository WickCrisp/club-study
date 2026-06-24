package com.crisp.demo.controller.admin;

import com.crisp.demo.common.Result;
import com.crisp.demo.entity.User;
import com.crisp.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    // ========== 后台 API ==========

    /** 用户列表 */
    @GetMapping("/api/user/list")
    public Result<List<User>> list() {
        return Result.success(userService.findAll());
    }

    /** 用户搜索 */
    @GetMapping("/api/user/search")
    public Result<List<User>> search(@RequestParam String keyword) {
        return Result.success(userService.search(keyword));
    }

    /** 用户详情 */
    @GetMapping("/api/user/{id}")
    public Result<User> getById(@PathVariable Long id) {
        return Result.success(userService.findById(id));
    }

    /** 删除用户 */
    @DeleteMapping("/api/user/{id}")
    public Result<?> delete(@PathVariable Long id) {
        userService.deleteById(id);
        return Result.success("删除成功");
    }
}
