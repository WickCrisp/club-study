package com.crisp.demo.controller.portal;

import com.crisp.demo.common.Result;
import com.crisp.demo.config.CurrentUser;
import com.crisp.demo.entity.User;
import com.crisp.demo.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequiredArgsConstructor
public class PortalUserController {

    private final UserService userService;

    // ========== 页面路由 ==========

    @GetMapping("/")
    public String index() {
        return "forward:/index.html";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "forward:/login.html";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "forward:/register.html";
    }

    @GetMapping("/user/profile")
    public String profilePage() {
        return "forward:/user/profile.html";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.removeAttribute("user");
        return "redirect:/login.html";
    }

    // ========== 前台 API ==========

    /** 退出登录（只清除 user，不影响后台 adminUser） */
    @PostMapping("/api/user/logout")
    public Result<?> apiLogout(HttpSession session) {
        session.removeAttribute("user");
        return Result.success("已退出登录");
    }

    /** 用户登录 */
    @PostMapping("/api/user/login")
    public Result<User> login(@RequestParam String studentNo, @RequestParam String password, HttpSession session) {
        User user = userService.login(studentNo, password);
        session.setAttribute("user", user);
        return Result.success("登录成功", user);
    }

    /** 用户注册 */
    @PostMapping("/api/user/register")
    public Result<User> register(@Valid @RequestBody User user) {
        User registeredUser = userService.register(user);
        return Result.success("注册成功", registeredUser);
    }

    /** 获取当前登录用户（不走拦截器，直接读 session） */
    @GetMapping("/api/user/current")
    public Result<User> currentUser(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return Result.error(401, "未登录");
        }
        return Result.success(user);
    }

    /** 更新个人信息 */
    @PutMapping("/api/user/update")
    public Result<?> update(@Valid @RequestBody User user, @CurrentUser User currentUser, HttpSession session) {
        user.setId(currentUser.getId());
        userService.update(user);
        User updatedUser = userService.findById(currentUser.getId());
        session.setAttribute("user", updatedUser);
        return Result.success("更新成功");
    }

    /** 修改密码 */
    @PutMapping("/api/user/updatePassword")
    public Result<?> updatePassword(@RequestParam String oldPassword, @RequestParam String newPassword, @CurrentUser User currentUser) {
        userService.updatePassword(currentUser.getId(), oldPassword, newPassword);
        return Result.success("密码修改成功");
    }
}
