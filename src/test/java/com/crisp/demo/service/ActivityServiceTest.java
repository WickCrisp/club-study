package com.crisp.demo.service;


import com.crisp.demo.domain.activity.app.ActivityService;
import com.crisp.demo.domain.club.app.ClubService;
import com.crisp.demo.domain.identity.app.UserService;
import com.crisp.demo.domain.membership.app.ClubMemberService;
import com.crisp.demo.domain.registration.app.ActivityRegistrationService;
import com.crisp.demo.TestDemoApplication;
import com.crisp.demo.domain.activity.model.Activity;
import com.crisp.demo.support.TestFixtures;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 活动模块测试 — CRUD、状态机
 * 测试数据由 TestFixtures 现场准备，不依赖数据库里的固定 id
 */
@SpringBootTest(classes = TestDemoApplication.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ActivityServiceTest {

    @Autowired
    private ActivityService activityService;

    @Autowired
    private UserService userService;

    @Autowired
    private ClubService clubService;

    private Long clubId;
    private Long creatorId;
    private static Long testActivityId;

    @BeforeAll
    void setUpFixtures() {
        creatorId = TestFixtures.ensureStudent(userService).getId();
        clubId = TestFixtures.ensureClub(clubService, creatorId).getId();
    }

    @Test
    @Order(1)
    @DisplayName("创建活动（草稿）")
    void testCreateDraft() {
        Activity activity = new Activity();
        activity.setClubId(clubId);
        activity.setTitle("JUnit测试活动");
        activity.setDescription("测试用活动");
        activity.setLocation("测试地点");
        activity.setStartTime(LocalDateTime.now().plusDays(1));
        activity.setEndTime(LocalDateTime.now().plusDays(1).plusHours(2));
        activity.setMaxParticipants(50);
        activity.setCreatorId(creatorId);
        activity.setStatus("DRAFT");

        Activity result = activityService.create(activity);
        assertNotNull(result.getId());
        assertEquals("DRAFT", result.getStatus());
        assertEquals(0, result.getCurrentCount());
        assertNotNull(result.getCreatedAt(), "创建后应回读数据库生成的创建时间");
        testActivityId = result.getId();
    }

    @Test
    @Order(2)
    @DisplayName("根据ID查询活动")
    void testFindById() {
        Activity activity = activityService.findById(testActivityId);
        assertNotNull(activity);
        assertEquals("JUnit测试活动", activity.getTitle());
    }

    @Test
    @Order(3)
    @DisplayName("活动列表不为空")
    void testFindAll() {
        List<Activity> list = activityService.findAll();
        assertFalse(list.isEmpty());
    }

    @Test
    @Order(4)
    @DisplayName("按社团筛选活动")
    void testFindByClubId() {
        List<Activity> list = activityService.findByClubId(clubId);
        assertFalse(list.isEmpty());
    }

    @Test
    @Order(5)
    @DisplayName("按状态筛选活动")
    void testFindByStatus() {
        List<Activity> list = activityService.findByStatus("DRAFT");
        assertFalse(list.isEmpty());
    }

    @Test
    @Order(6)
    @DisplayName("搜索活动")
    void testSearch() {
        List<Activity> list = activityService.search("JUnit");
        assertFalse(list.isEmpty());
    }

    @Test
    @Order(7)
    @DisplayName("更新活动信息")
    void testUpdate() {
        Activity activity = activityService.findById(testActivityId);
        activity.setTitle("更新后的活动名");
        Activity updated = activityService.update(activity);
        assertEquals("更新后的活动名", updated.getTitle());
    }

    @Test
    @Order(8)
    @DisplayName("活动状态变更 — DRAFT → PENDING → ONGOING（必须经审核）")
    void testUpdateStatus() {
        Activity submitted = activityService.submitReview(testActivityId);
        assertEquals("PENDING", submitted.getStatus(), "提交审核后应为待审核");

        activityService.review(testActivityId, true);
        Activity updated = activityService.findById(testActivityId);
        assertEquals("ONGOING", updated.getStatus(), "审核通过后应为进行中");
    }

    @Test
    @Order(9)
    @DisplayName("非法状态流转 — DRAFT → FINISHED 应被拒绝")
    void testIllegalTransition() {
        Long anotherId = activityService.create(draftActivity("JUnit非法流转活动")).getId();
        assertThrows(RuntimeException.class,
                () -> activityService.updateStatus(anotherId, "FINISHED"),
                "DRAFT 不允许直接跳到 FINISHED");
        activityService.deleteById(anotherId);
    }

    @Test
    @Order(10)
    @DisplayName("活动状态变更 — ONGOING → FINISHED")
    void testUpdateStatusFinish() {
        activityService.updateStatus(testActivityId, "FINISHED");
        Activity updated = activityService.findById(testActivityId);
        assertEquals("FINISHED", updated.getStatus());
    }

    @Test
    @Order(11)
    @DisplayName("结束时间早于开始时间应被拒绝")
    void testInvalidTimeRange() {
        Activity activity = draftActivity("JUnit时间非法活动");
        activity.setStartTime(LocalDateTime.now().plusDays(2));
        activity.setEndTime(LocalDateTime.now().plusDays(1));
        assertThrows(RuntimeException.class, () -> activityService.create(activity));
    }

    @Test
    @Order(12)
    @DisplayName("删除活动")
    void testDelete() {
        boolean result = activityService.deleteById(testActivityId);
        assertTrue(result);
        assertThrows(RuntimeException.class,
                () -> activityService.findById(testActivityId));
    }

    @Test
    @Order(13)
    @DisplayName("未指定参与范围时默认为公开活动")
    void testJoinScopeDefaultPublic() {
        Activity activity = draftActivity("JUnit默认参与范围");
        activity.setJoinScope(null);
        Activity created = activityService.create(activity);
        assertEquals("PUBLIC", created.getJoinScope());
        activityService.deleteById(created.getId());
    }

    @Test
    @Order(14)
    @DisplayName("非法参与范围应被拒绝")
    void testInvalidJoinScope() {
        Activity activity = draftActivity("JUnit非法参与范围");
        activity.setJoinScope("EVERYONE");
        assertThrows(RuntimeException.class, () -> activityService.create(activity));
    }

    private Activity draftActivity(String title) {        Activity activity = new Activity();
        activity.setClubId(clubId);
        activity.setTitle(title);
        activity.setDescription("测试用");
        activity.setLocation("测试地点");
        activity.setStartTime(LocalDateTime.now().plusDays(1));
        activity.setEndTime(LocalDateTime.now().plusDays(1).plusHours(2));
        activity.setMaxParticipants(10);
        activity.setCreatorId(creatorId);
        activity.setStatus("DRAFT");
        return activity;
    }
}
