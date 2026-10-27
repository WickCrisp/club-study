package com.crisp.demo.service;


import com.crisp.demo.domain.activity.app.ActivityService;
import com.crisp.demo.domain.club.app.ClubService;
import com.crisp.demo.domain.identity.app.UserService;
import com.crisp.demo.domain.membership.app.ClubMemberService;
import com.crisp.demo.domain.registration.app.ActivityRegistrationService;
import com.crisp.demo.TestDemoApplication;
import com.crisp.demo.domain.identity.model.User;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 用户模块测试 — 注册、登录、修改密码
 * 使用独立测试账号，运行前后自清理，不依赖数据库里的固定 id
 */
@SpringBootTest(classes = TestDemoApplication.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserServiceTest {

    private static final String TEST_STUDENT_NO = "test9999";
    private static final String TEST_PASSWORD = "123456";
    private static Long testUserId;

    @Autowired
    private UserService userService;

    @BeforeAll
    void cleanUpLeftover() {
        User exist = userService.findByStudentNo(TEST_STUDENT_NO);
        if (exist != null) {
            userService.deleteById(exist.getId());   // 清理上次失败运行的残留
        }
    }

    @Test
    @Order(1)
    @DisplayName("注册新用户")
    void testRegister() {
        User user = new User();
        user.setStudentNo(TEST_STUDENT_NO);
        user.setPassword(TEST_PASSWORD);
        user.setName("测试用户");
        user.setPhone("13800000001");
        user.setEmail("test@test.com");
        user.setGender(1);
        user.setRole("STUDENT");

        User result = userService.register(user);
        assertNotNull(result.getId(), "注册后应回填 ID");
        assertEquals(TEST_STUDENT_NO, result.getStudentNo());
        testUserId = result.getId();
    }

    @Test
    @Order(2)
    @DisplayName("学号重复注册应失败")
    void testDuplicateRegister() {
        User user = new User();
        user.setStudentNo(TEST_STUDENT_NO);
        user.setPassword("111111");
        user.setName("重复用户");

        assertThrows(RuntimeException.class, () -> userService.register(user),
                "重复学号应抛出异常");
    }

    @Test
    @Order(3)
    @DisplayName("正确密码登录成功")
    void testLoginSuccess() {
        User user = userService.login(TEST_STUDENT_NO, TEST_PASSWORD);
        assertNotNull(user);
        assertEquals(TEST_STUDENT_NO, user.getStudentNo());
    }

    @Test
    @Order(4)
    @DisplayName("错误密码登录失败")
    void testLoginWrongPassword() {
        assertThrows(RuntimeException.class,
                () -> userService.login(TEST_STUDENT_NO, "wrongpwd"),
                "错误密码应抛出异常");
    }

    @Test
    @Order(5)
    @DisplayName("不存在的用户登录失败")
    void testLoginNonexistent() {
        assertThrows(RuntimeException.class,
                () -> userService.login("nobody999", "123456"),
                "不存在用户应抛出异常");
    }

    @Test
    @Order(6)
    @DisplayName("修改个人信息")
    void testUpdate() {
        User user = userService.findById(testUserId);
        user.setName("修改后姓名");
        user.setPhone("13900000000");
        boolean result = userService.update(user);
        assertTrue(result);

        User updated = userService.findById(testUserId);
        assertEquals("修改后姓名", updated.getName());
    }

    @Test
    @Order(7)
    @DisplayName("修改密码 — 旧密码错误应失败")
    void testUpdatePasswordWrongOld() {
        assertThrows(RuntimeException.class,
                () -> userService.updatePassword(testUserId, "wrongold", "newpass"),
                "旧密码错误应抛出异常");
    }

    @Test
    @Order(8)
    @DisplayName("修改密码 — 成功")
    void testUpdatePasswordSuccess() {
        boolean result = userService.updatePassword(testUserId, TEST_PASSWORD, "newpass123");
        assertTrue(result);
    }

    @Test
    @Order(9)
    @DisplayName("按学号查找用户")
    void testFindByStudentNo() {
        User user = userService.findByStudentNo(TEST_STUDENT_NO);
        assertNotNull(user);
        assertEquals(TEST_STUDENT_NO, user.getStudentNo());
    }

    @Test
    @Order(10)
    @DisplayName("用户列表不为空")
    void testFindAll() {
        assertFalse(userService.findAll().isEmpty(), "用户列表不应为空");
    }

    @Test
    @Order(11)
    @DisplayName("搜索用户")
    void testSearch() {
        var result = userService.search(TEST_STUDENT_NO);
        assertFalse(result.isEmpty(), "搜索学号应有结果");
    }

    @AfterAll
    void cleanup() {
        if (testUserId != null) {
            userService.deleteById(testUserId);
        }
    }
}
