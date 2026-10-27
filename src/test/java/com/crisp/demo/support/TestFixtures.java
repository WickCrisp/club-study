package com.crisp.demo.support;

import com.crisp.demo.domain.club.model.Club;
import com.crisp.demo.domain.identity.model.User;
import com.crisp.demo.domain.club.app.ClubService;
import com.crisp.demo.domain.identity.app.UserService;

/**
 * 测试夹具 — 让测试自己准备数据，不再依赖数据库里预先存在的固定 id
 *
 * 所有 ensureXxx 方法都是幂等的：存在就直接复用，不存在才创建。
 */
public final class TestFixtures {

    /** 测试用学生账号 */
    public static final String STUDENT_NO = "junit0001";
    /** 测试用社团名称 */
    public static final String CLUB_NAME = "JUnit测试社团";

    private TestFixtures() {
    }

    /** 确保存在指定账号的用户 */
    public static User ensureUser(UserService userService, String studentNo, String name, String role) {
        User exist = userService.findByStudentNo(studentNo);
        if (exist != null) {
            return exist;
        }
        User user = new User();
        user.setStudentNo(studentNo);
        user.setPassword("123456");
        user.setName(name);
        user.setGender(1);
        user.setRole(role);
        return userService.register(user);
    }

    /** 确保存在测试学生 */
    public static User ensureStudent(UserService userService) {
        return ensureUser(userService, STUDENT_NO, "JUnit测试学生", "STUDENT");
    }

    /** 确保存在测试社团（负责人为指定用户） */
    public static Club ensureClub(ClubService clubService, Long leaderId) {
        return clubService.search(CLUB_NAME).stream()
                .filter(c -> CLUB_NAME.equals(c.getName()))
                .findFirst()
                .orElseGet(() -> {
                    Club club = new Club();
                    club.setName(CLUB_NAME);
                    club.setDescription("自动化测试用社团");
                    club.setLeaderId(leaderId);
                    club.setStatus("ACTIVE");
                    return clubService.create(club);
                });
    }

    /** 清理上一次失败运行可能残留的同名社团 */
    public static void deleteClubIfExists(ClubService clubService, String name) {
        clubService.search(name).stream()
                .filter(c -> name.equals(c.getName()))
                .forEach(c -> clubService.deleteById(c.getId()));
    }
}
