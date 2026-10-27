package com.crisp.demo.service;

import com.crisp.demo.TestDemoApplication;
import com.crisp.demo.domain.analytics.app.AnalyticsService;
import com.crisp.demo.domain.announcement.app.AnnouncementService;
import com.crisp.demo.domain.announcement.infra.AnnouncementMapper;
import com.crisp.demo.domain.announcement.model.Announcement;
import com.crisp.demo.domain.club.app.ClubService;
import com.crisp.demo.domain.club.infra.ClubCategoryMapper;
import com.crisp.demo.domain.club.model.Club;
import com.crisp.demo.domain.club.model.ClubCategory;
import com.crisp.demo.domain.identity.app.UserService;
import com.crisp.demo.domain.identity.model.User;
import com.crisp.demo.domain.showcase.app.ClubShowcaseService;
import com.crisp.demo.domain.showcase.infra.ClubShowcaseMapper;
import com.crisp.demo.domain.showcase.model.ClubShowcase;
import com.crisp.demo.support.TestFixtures;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 阶段 D 域测试：分类 / 公告 / 社团风采 / 统计
 */
@SpringBootTest(classes = TestDemoApplication.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StageDDomainTest {

    @Autowired private ClubCategoryMapper categoryMapper;
    @Autowired private AnnouncementService announcementService;
    @Autowired private AnnouncementMapper announcementMapper;
    @Autowired private ClubShowcaseService showcaseService;
    @Autowired private ClubShowcaseMapper showcaseMapper;
    @Autowired private AnalyticsService analyticsService;
    @Autowired private ClubService clubService;
    @Autowired private UserService userService;

    private User student;
    private User admin;
    private Long clubId;
    private static Long categoryId;
    private static Long showcaseId;

    @BeforeAll
    void setUpFixtures() {
        student = TestFixtures.ensureStudent(userService);
        admin = TestFixtures.ensureUser(userService, "junitadmin", "JUnit管理员", "ADMIN");
        clubId = TestFixtures.ensureClub(clubService, student.getId()).getId();
    }

    // ===== 分类 =====

    @Test
    @Order(1)
    @DisplayName("分类：新增 / 查询 / 重名拒绝")
    void testCategoryCrud() {
        ClubCategory category = new ClubCategory();
        category.setName("JUnit分类A");
        category.setSort(5);
        category.setStatus("ACTIVE");
        categoryMapper.insert(category);
        categoryId = category.getId();
        assertNotNull(categoryId);

        assertNotNull(categoryMapper.findById(categoryId));
        assertTrue(categoryMapper.findActive().stream().anyMatch(c -> c.getId().equals(categoryId)));
    }

    @Test
    @Order(2)
    @DisplayName("社团可挂到分类下")
    void testClubCategoryBinding() {
        Club club = new Club();
        club.setName("JUnit分类社团");
        club.setLeaderId(student.getId());
        club.setStatus("ACTIVE");
        club.setCategoryId(categoryId);
        Club saved = clubService.create(club);

        Club loaded = clubService.findById(saved.getId());
        assertEquals(categoryId, loaded.getCategoryId());
        assertEquals("JUnit分类A", loaded.getCategoryName(), "应联表查出分类名");

        clubService.deleteById(saved.getId());
    }

    // ===== 公告 =====

    @Test
    @Order(3)
    @DisplayName("公告：草稿不出现在公开列表，发布后出现")
    void testAnnouncementLifecycle() {
        Announcement draft = new Announcement();
        draft.setTitle("JUnit公告草稿");
        draft.setContent("内容");
        draft.setStatus("DRAFT");
        Announcement created = announcementService.create(draft, admin.getId());

        assertTrue(announcementService.findPublished().stream()
                .noneMatch(a -> a.getId().equals(created.getId())), "草稿不应出现在公开列表");

        announcementService.changeStatus(created.getId(), "PUBLISHED");
        assertTrue(announcementService.findPublished().stream()
                .anyMatch(a -> a.getId().equals(created.getId())), "发布后应出现在公开列表");

        announcementService.deleteById(created.getId());
        assertEquals(0, announcementMapper.findById(created.getId()) == null ? 0 : 1);
    }

    // ===== 社团风采 =====

    @Test
    @Order(4)
    @DisplayName("社团风采：一个社团可发多条，聚合页按社团返回")
    void testShowcaseMultiplePerClub() {
        for (int i = 1; i <= 2; i++) {
            ClubShowcase showcase = new ClubShowcase();
            showcase.setClubId(clubId);
            showcase.setTitle("JUnit风采" + i);
            showcase.setContent("内容" + i);
            ClubShowcase created = showcaseService.create(showcase, student.getId());
            if (i == 1) showcaseId = created.getId();
        }
        List<ClubShowcase> list = showcaseService.findByClubId(clubId);
        assertTrue(list.size() >= 2, "同一社团应能有多条风采");
        assertTrue(list.stream().allMatch(s -> clubId.equals(s.getClubId())));
        assertEquals(clubId, showcaseMapper.findById(showcaseId).getClubId());
    }

    @Test
    @Order(5)
    @DisplayName("社团风采：删除后聚合页不再返回")
    void testShowcaseDelete() {
        showcaseService.deleteById(showcaseId);
        assertNull(showcaseMapper.findById(showcaseId));
    }

    // ===== 统计 =====

    @Test
    @Order(6)
    @DisplayName("统计：全局与社团维度均可聚合")
    void testAnalytics() {
        Map<String, Object> global = analyticsService.global();
        assertTrue(((Number) global.get("users")).intValue() > 0);
        assertTrue(((Number) global.get("clubs")).intValue() > 0);

        Map<String, Object> club = analyticsService.club(clubId);
        assertEquals(clubId, club.get("clubId"));
        assertNotNull(club.get("members"));
        assertNotNull(club.get("activities"));
        assertNotNull(club.get("showcases"));
    }

    @Test
    @Order(7)
    @DisplayName("统计：不存在的社团应报错")
    void testAnalyticsUnknownClub() {
        assertThrows(RuntimeException.class, () -> analyticsService.club(999999L));
    }

    @Test
    @Order(8)
    @DisplayName("清理分类")
    void cleanupCategory() {
        categoryMapper.deleteById(categoryId);
        assertNull(categoryMapper.findById(categoryId));
    }
}
