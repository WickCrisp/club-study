package com.crisp.demo.controller.admin;

import com.crisp.demo.common.Result;
import com.crisp.demo.config.CurrentUser;
import com.crisp.demo.entity.Club;
import com.crisp.demo.entity.ClubMember;
import com.crisp.demo.entity.User;
import com.crisp.demo.mapper.ClubMapper;
import com.crisp.demo.mapper.ClubMemberMapper;
import com.crisp.demo.mapper.UserMapper;
import com.crisp.demo.service.ClubService;
import com.crisp.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AdminClubController {

    private final ClubService clubService;
    private final ClubMapper clubMapper;
    private final UserMapper userMapper;
    private final ClubMemberMapper clubMemberMapper;
    private final UserService userService;

    // ========== 后台 API ==========

    /** 管理员登录 */
    @PostMapping("/api/admin/login")
    public Result<User> login(@RequestParam String studentNo, @RequestParam String password,
                              HttpSession session) {
        User user = userMapper.findByStudentNo(studentNo);
        if (user == null) {
            return Result.error("用户不存在");
        }
        String encrypted = org.springframework.util.DigestUtils
                .md5DigestAsHex(password.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        if (!encrypted.equals(user.getPassword())) {
            return Result.error("密码错误");
        }
        session.setAttribute("user", user);
        return Result.success("登录成功", user);
    }

    /** 获取当前管理员 */
    @GetMapping("/api/admin/current")
    public Result<User> currentAdmin(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return Result.error(401, "未登录");
        return Result.success(user);
    }

    /** 管理员更新个人信息 */
    @PutMapping("/api/admin/update")
    public Result<?> update(@Valid @RequestBody User user, HttpSession session) {
        User u = (User) session.getAttribute("user");
        if (u == null) return Result.error(401, "未登录");
        user.setId(u.getId());
        userService.update(user);
        User updated = userService.findById(u.getId());
        session.setAttribute("user", updated);
        return Result.success("更新成功");
    }

    /** 管理员修改密码 */
    @PutMapping("/api/admin/updatePassword")
    public Result<?> updatePassword(@RequestParam String oldPassword, @RequestParam String newPassword,
                                     HttpSession session) {
        User u = (User) session.getAttribute("user");
        if (u == null) return Result.error(401, "未登录");
        userService.updatePassword(u.getId(), oldPassword, newPassword);
        return Result.success("密码修改成功");
    }

    /** 管理员登出 */
    @PostMapping("/api/admin/logout")
    public Result<?> logout(HttpSession session) {
        session.removeAttribute("user");
        return Result.success("已退出登录");
    }

    /** 我管理的社团（管理员可见全部社团） */
    @GetMapping("/api/club/my-managed")
    public Result<List<Club>> myManaged(@CurrentUser User user) {
        if ("ADMIN".equals(user.getRole())) {
            return Result.success(clubMapper.findAll());
        }
        return Result.success(clubMapper.findByLeaderId(user.getId()));
    }

    /** 创建社团，自动将负责人添加为社团 LEADER */
    @PostMapping("/api/club/create")
    public Result<Club> create(@Valid @RequestBody Map<String, Object> body) {
        Club club = buildClub(body);
        club = clubService.create(club);
        // 创建社团后自动添加负责人为已通过的 LEADER 成员
        if (club.getLeaderId() != null) {
            ClubMember exist = clubMemberMapper.findByUserAndClub(club.getLeaderId(), club.getId());
            if (exist != null) {
                // 已在 club_member 中有记录（如之前申请过）→ 升级为 LEADER + APPROVED
                exist.setRole("LEADER");
                exist.setStatus("APPROVED");
                clubMemberMapper.update(exist);
            } else {
                ClubMember leader = new ClubMember();
                leader.setUserId(club.getLeaderId());
                leader.setClubId(club.getId());
                leader.setRole("LEADER");
                leader.setStatus("APPROVED");
                clubMemberMapper.insert(leader);
            }
        }
        return Result.success("创建成功", club);
    }

    /** 更新社团 */
    @PutMapping("/api/club/update")
    public Result<Club> update(@Valid @RequestBody Map<String, Object> body) {
        Club club = buildClub(body);
        return Result.success("更新成功", clubService.update(club));
    }

    /** 删除社团 */
    @DeleteMapping("/api/club/{id}")
    public Result<?> delete(@PathVariable Long id) {
        clubService.deleteById(id);
        return Result.success("删除成功");
    }

    /** 从请求体构建 Club 对象，学号 → 用户 ID */
    private Club buildClub(Map<String, Object> body) {
        Club club = new Club();
        if (body.get("id") != null) club.setId(Long.valueOf(body.get("id").toString()));
        club.setName((String) body.get("name"));
        club.setDescription((String) body.get("description"));
        club.setStatus((String) body.get("status"));

        // 负责人学号 → 查询用户 ID
        String leaderStudentNo = (String) body.get("leaderStudentNo");
        if (leaderStudentNo != null && !leaderStudentNo.isEmpty()) {
            User leader = userMapper.findByStudentNo(leaderStudentNo);
            if (leader != null) {
                club.setLeaderId(leader.getId());
            }
        }
        return club;
    }
}
