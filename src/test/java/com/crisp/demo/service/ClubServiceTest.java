package com.crisp.demo.service;


import com.crisp.demo.domain.activity.app.ActivityService;
import com.crisp.demo.domain.club.app.ClubService;
import com.crisp.demo.domain.identity.app.UserService;
import com.crisp.demo.domain.membership.app.ClubMemberService;
import com.crisp.demo.domain.registration.app.ActivityRegistrationService;
import com.crisp.demo.TestDemoApplication;
import com.crisp.demo.domain.club.model.Club;
import com.crisp.demo.support.TestFixtures;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 社团模块测试 — CRUD、搜索
 * 测试数据由 TestFixtures 现场准备，不依赖数据库里的固定 id
 */
@SpringBootTest(classes = TestDemoApplication.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ClubServiceTest {

    private static final String TEST_CLUB_NAME = "测试社团JUnit";

    @Autowired
    private ClubService clubService;

    @Autowired
    private UserService userService;

    private Long leaderId;
    private static Long testClubId;

    @BeforeAll
    void setUpFixtures() {
        leaderId = TestFixtures.ensureStudent(userService).getId();
        TestFixtures.deleteClubIfExists(clubService, TEST_CLUB_NAME);   // 清理上次失败运行的残留
    }

    @Test
    @Order(1)
    @DisplayName("创建社团")
    void testCreate() {
        Club club = new Club();
        club.setName(TEST_CLUB_NAME);
        club.setDescription("JUnit 测试用社团");
        club.setLeaderId(leaderId);
        club.setStatus("ACTIVE");

        Club result = clubService.create(club);
        assertNotNull(result.getId());
        assertEquals(TEST_CLUB_NAME, result.getName());
        assertNotNull(result.getCreatedAt(), "创建后应回读数据库生成的创建时间");
        testClubId = result.getId();
    }

    @Test
    @Order(2)
    @DisplayName("根据ID查询社团")
    void testFindById() {
        Club club = clubService.findById(testClubId);
        assertNotNull(club);
        assertEquals(TEST_CLUB_NAME, club.getName());
    }

    @Test
    @Order(3)
    @DisplayName("查询不存在的社团应失败")
    void testFindByIdNotFound() {
        assertThrows(RuntimeException.class,
                () -> clubService.findById(99999L),
                "不存在社团应抛出异常");
    }

    @Test
    @Order(4)
    @DisplayName("社团列表不为空")
    void testFindAll() {
        List<Club> clubs = clubService.findAll();
        assertFalse(clubs.isEmpty());
    }

    @Test
    @Order(5)
    @DisplayName("搜索社团")
    void testSearch() {
        List<Club> result = clubService.search(TEST_CLUB_NAME);
        assertFalse(result.isEmpty());
    }

    @Test
    @Order(6)
    @DisplayName("更新社团信息")
    void testUpdate() {
        Club club = clubService.findById(testClubId);
        club.setDescription("更新后的描述");
        Club updated = clubService.update(club);
        assertEquals("更新后的描述", updated.getDescription());
    }

    @Test
    @Order(7)
    @DisplayName("重名社团创建应失败")
    void testCreateDuplicateName() {
        Club club = new Club();
        club.setName(TEST_CLUB_NAME);
        club.setDescription("重名");
        club.setLeaderId(leaderId);
        club.setStatus("ACTIVE");
        assertThrows(RuntimeException.class, () -> clubService.create(club),
                "重名社团应抛出业务异常");
    }

    @Test
    @Order(8)
    @DisplayName("删除社团")
    void testDelete() {
        boolean result = clubService.deleteById(testClubId);
        assertTrue(result);
        assertThrows(RuntimeException.class,
                () -> clubService.findById(testClubId));
    }
}
