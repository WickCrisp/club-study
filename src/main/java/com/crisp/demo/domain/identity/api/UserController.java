package com.crisp.demo.domain.identity.api;

import com.crisp.demo.domain.identity.api.dto.LoginResponse;
import com.crisp.demo.shared.error.UnauthorizedException;
import com.crisp.demo.shared.result.Result;
import com.crisp.demo.shared.security.ClientIp;
import com.crisp.demo.shared.security.CurrentUser;
import com.crisp.demo.shared.security.JwtService;
import com.crisp.demo.shared.security.LoginAttemptService;
import com.crisp.demo.domain.identity.model.User;
import com.crisp.demo.domain.identity.app.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 前台账号接口
 *
 * 认证方式为 JWT：登录签发令牌，之后每个请求由 JwtAuthInterceptor 校验，
 * 服务端不保存登录态，因此登出只需前端丢弃令牌。
 */
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtService jwtService;
    private final LoginAttemptService loginAttemptService;

    // ========== 前台 API ==========

    /** 退出登录（无状态令牌，服务端无需清理，保留接口供前端调用） */
    @PostMapping("/api/user/logout")
    public Result<?> apiLogout() {
        return Result.success("已退出登录");
    }

    /** 用户登录 → 签发令牌（连续失败会被限流） */
    @PostMapping("/api/user/login")
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

        return Result.success("登录成功", new LoginResponse(
                jwtService.issue(user), jwtService.getExpireSeconds(), user));
    }

    /** 用户注册 */
    @PostMapping("/api/user/register")
    public Result<User> register(@Valid @RequestBody User user) {
        User registeredUser = userService.register(user);
        return Result.success("注册成功", registeredUser);
    }

    /** 获取当前登录用户 — 读数据库保证资料是最新的，令牌只用于确认身份 */
    @GetMapping("/api/user/current")
    public Result<User> currentUser(@CurrentUser User tokenUser) {
        if (tokenUser == null) {
            throw new UnauthorizedException("未登录");
        }
        User fresh = userService.findById(tokenUser.getId());
        return Result.success(fresh == null ? tokenUser : fresh);
    }

    /** 更新个人信息 */
    @PutMapping("/api/user/update")
    public Result<?> update(@Valid @RequestBody User user, @CurrentUser User currentUser) {
        user.setId(currentUser.getId());
        userService.update(user);
        return Result.success("更新成功");
    }

    /** 修改密码 */
    @PutMapping("/api/user/updatePassword")
    public Result<?> updatePassword(@RequestParam String oldPassword, @RequestParam String newPassword,
                                    @CurrentUser User currentUser) {
        userService.updatePassword(currentUser.getId(), oldPassword, newPassword);
        return Result.success("密码修改成功");
    }
}
