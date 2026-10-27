package com.crisp.demo.domain.identity.api;

import com.crisp.demo.domain.identity.app.UserService;
import com.crisp.demo.domain.identity.model.User;
import com.crisp.demo.shared.result.Result;
import com.crisp.demo.shared.security.RequireRole;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户管理 — 仅管理员可访问（密码字段已标记 WRITE_ONLY，不会出现在响应中）
 *
 * 四个接口都是"只有 ADMIN 能进"，因此用类级 @RequireRole 在进入方法前统一判定，
 * 不必在每个方法里重复 Permission.requireAdmin。
 */
@RestController
@RequiredArgsConstructor
@RequireRole(value = "ADMIN", action = "用户管理")
public class UserAdminController {

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
