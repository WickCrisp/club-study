package com.crisp.demo.controller;

import com.crisp.demo.common.Result;
import com.crisp.demo.entity.User;
import com.crisp.demo.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

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
        session.invalidate();
        return "redirect:/login.html";
    }

    @PostMapping("/api/user/login")
    @ResponseBody
    public Result<User> login(@RequestParam String studentNo, @RequestParam String password, HttpSession session) {
        try {
            User user = userService.login(studentNo, password);
            session.setAttribute("user", user);
            return Result.success("登录成功", user);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/api/user/register")
    @ResponseBody
    public Result<User> register(@RequestBody User user) {
        try {
            User registeredUser = userService.register(user);
            return Result.success("注册成功", registeredUser);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/api/user/current")
    @ResponseBody
    public Result<User> currentUser(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return Result.error(401, "未登录");
        }
        return Result.success(user);
    }

    @GetMapping("/api/user/list")
    @ResponseBody
    public Result<List<User>> list() {
        return Result.success(userService.findAll());
    }

    @GetMapping("/api/user/search")
    @ResponseBody
    public Result<List<User>> search(@RequestParam String keyword) {
        return Result.success(userService.search(keyword));
    }

    @GetMapping("/api/user/{id}")
    @ResponseBody
    public Result<User> getById(@PathVariable Long id) {
        try {
            return Result.success(userService.findById(id));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/api/user/update")
    @ResponseBody
    public Result<?> update(@RequestBody User user, HttpSession session) {
        try {
            User currentUser = (User) session.getAttribute("user");
            if (currentUser == null) {
                return Result.error(401, "未登录");
            }
            user.setId(currentUser.getId());
            userService.update(user);
            User updatedUser = userService.findById(currentUser.getId());
            session.setAttribute("user", updatedUser);
            return Result.success("更新成功");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/api/user/updatePassword")
    @ResponseBody
    public Result<?> updatePassword(@RequestParam String oldPassword, @RequestParam String newPassword, HttpSession session) {
        try {
            User currentUser = (User) session.getAttribute("user");
            if (currentUser == null) {
                return Result.error(401, "未登录");
            }
            userService.updatePassword(currentUser.getId(), oldPassword, newPassword);
            return Result.success("密码修改成功");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/api/user/{id}")
    @ResponseBody
    public Result<?> delete(@PathVariable Long id) {
        try {
            userService.deleteById(id);
            return Result.success("删除成功");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}