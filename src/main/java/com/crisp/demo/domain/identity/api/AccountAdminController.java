package com.crisp.demo.domain.identity.api;

import com.crisp.demo.domain.identity.api.dto.LoginResponse;
import com.crisp.demo.domain.identity.app.UserService;
import com.crisp.demo.domain.identity.model.User;
import com.crisp.demo.shared.error.ForbiddenException;
import com.crisp.demo.shared.error.UnauthorizedException;
import com.crisp.demo.shared.security.ClientIp;
import com.crisp.demo.shared.security.LoginAttemptService;
import com.crisp.demo.shared.security.RequireRole;
import com.crisp.demo.shared.result.Result;
import com.crisp.demo.shared.security.CurrentUser;
import com.crisp.demo.shared.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 管理端账号 — 与学生端共用同一套账号与令牌
 *
 * 后台只对管理员开放：登录时校验 role=ADMIN，普通学生即使密码正确也拿不到令牌；
 * current 接口再加一层 @RequireRole，防止拿着学生端令牌直接访问后台。
 */
@RestController
@RequiredArgsConstructor
public class AccountAdminController {

    private final UserService userService;
    private final JwtService jwtService;
    private final LoginAttemptService loginAttemptService;

    /** 管理端登录 → 仅管理员可登录，校验通过后签发令牌（连续失败会被限流） */
    @PostMapping("/api/admin/login")
    public Result<LoginResponse> login(@RequestParam String studentNo, @RequestParam String password,
                                       HttpServletRequest request) {
        String ip = ClientIp.resolve(request);
        loginAttemptService.checkAllowed(studentNo, ip);

        User user;
        try {
            user = userService.login(studentNo, password);
        } catch (RuntimeException e) {
            loginAttemptService.onFailure(studentNo, ip);
            throw e;
        }
        loginAttemptService.onSuccess(studentNo);

        if (!"ADMIN".equals(user.getRole())) {
            throw new ForbiddenException("仅管理员可登录后台");
        }
        return Result.success("登录成功", new LoginResponse(
                jwtService.issue(user), jwtService.getExpireSeconds(), user));
    }

    /** 获取当前登录用户（仅管理员，防止学生端令牌越权访问后台） */
    @GetMapping("/api/admin/current")
    @RequireRole(value = "ADMIN", action = "访问后台")
    public Result<User> currentAdmin(@CurrentUser User tokenUser) {
        if (tokenUser == null) {
            throw new UnauthorizedException("未登录");
        }
        User fresh = userService.findById(tokenUser.getId());
        return Result.success(fresh == null ? tokenUser : fresh);
    }

    /** 更新个人信息 */
    @PutMapping("/api/admin/update")
    public Result<?> update(@Valid @RequestBody User user, @CurrentUser User currentUser) {
        user.setId(currentUser.getId());
        userService.update(user);
        return Result.success("更新成功");
    }

    /** 修改密码 */
    @PutMapping("/api/admin/updatePassword")
    public Result<?> updatePassword(@RequestParam String oldPassword, @RequestParam String newPassword,
                                    @CurrentUser User currentUser) {
        userService.updatePassword(currentUser.getId(), oldPassword, newPassword);
        return Result.success("密码修改成功");
    }

    /** 登出（无状态令牌，服务端不保存登录态） */
    @PostMapping("/api/admin/logout")
    public Result<?> logout() {
        return Result.success("已退出登录");
    }
}
