package com.crisp.demo.service;


import com.crisp.demo.domain.activity.app.ActivityService;
import com.crisp.demo.domain.club.app.ClubService;
import com.crisp.demo.domain.identity.app.UserService;
import com.crisp.demo.domain.membership.app.ClubMemberService;
import com.crisp.demo.domain.registration.app.ActivityRegistrationService;
import com.crisp.demo.TestDemoApplication;
import com.crisp.demo.domain.activity.model.Activity;
import com.crisp.demo.domain.registration.model.ActivityRegistration;
import com.crisp.demo.support.TestFixtures;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 活动报名模块测试 — 报名、名额控制、取消审批
 * 测试数据由 TestFixtures 现场准备，不依赖数据库里的固定 id
 */
@SpringBootTest(classes = TestDemoApplication.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ActivityRegistrationServiceTest {

    @Autowired
    private ActivityRegistrationService registrationService;

    @Autowired
    private ActivityService activityService;

    @Autowired
    private UserService userService;

    @Autowired
    private ClubService clubService;

    private Long testUserId;
    private Long anotherUserId;
    private Long clubId;
    private static Long testActivityId;
    private static Long registrationId;

    @BeforeAll
    void setUpFixtures() {
        testUserId = TestFixtures.ensureStudent(userService).getId();
        anotherUserId = TestFixtures.ensureUser(userService, "junit0002", "JUnit测试学生2", "STUDENT").getId();
        clubId = TestFixtures.ensureClub(clubService, testUserId).getId();
    }

    @Test
    @Order(1)
    @DisplayName("创建并发布测试活动（限 1 人）")
    void createTestActivity() {
        Activity act = new Activity();
        act.setClubId(clubId);
        act.setTitle("JUnit报名测试活动");
        act.setDescription("测试用");
        act.setLocation("测试地点");
        act.setStartTime(LocalDateTime.now().plusDays(1));
        act.setEndTime(LocalDateTime.now().plusDays(1).plusHours(2));
        act.setMaxParticipants(1);
        act.setCreatorId(testUserId);
        act.setStatus("DRAFT");
        Activity result = activityService.create(act);
        testActivityId = result.getId();
        activityService.submitReview(testActivityId);
        activityService.review(testActivityId, true);
    }

    @Test
    @Order(2)
    @DisplayName("草稿活动不可报名")
    void testRegisterDraftRejected() {
        Activity draft = new Activity();
        draft.setClubId(clubId);
        draft.setTitle("JUnit草稿活动");
        draft.setLocation("测试地点");
        draft.setStartTime(LocalDateTime.now().plusDays(1));
        draft.setEndTime(LocalDateTime.now().plusDays(1).plusHours(2));
        draft.setMaxParticipants(10);
        draft.setCreatorId(testUserId);
        draft.setStatus("DRAFT");
        Long draftId = activityService.create(draft).getId();
        assertThrows(RuntimeException.class,
                () -> registrationService.register(draftId, testUserId),
                "草稿状态不可报名");
        activityService.deleteById(draftId);
    }

    @Test
    @Order(3)
    @DisplayName("报名活动")
    void testRegister() {
        ActivityRegistration reg = registrationService.register(testActivityId, testUserId);
        assertNotNull(reg.getId());
        assertEquals("REGISTERED", reg.getStatus());
        registrationId = reg.getId();
    }

    @Test
    @Order(4)
    @DisplayName("重复报名应失败")
    void testDuplicateRegister() {
        assertThrows(RuntimeException.class,
                () -> registrationService.register(testActivityId, testUserId),
                "重复报名应抛出异常");
    }

    @Test
    @Order(5)
    @DisplayName("名额已满时其他用户报名应失败，且人数不被多扣")
    void testRegisterWhenFull() {
        assertThrows(RuntimeException.class,
                () -> registrationService.register(testActivityId, anotherUserId),
                "名额已满应抛出异常");
        Activity activity = activityService.findById(testActivityId);
        assertEquals(1, activity.getCurrentCount(), "满员后 current_count 不应超过上限");
    }

    @Test
    @Order(6)
    @DisplayName("查询用户报名记录")
    void testFindByUserId() {
        List<ActivityRegistration> list = registrationService.findByUserId(testUserId);
        assertFalse(list.isEmpty());
        assertTrue(list.stream().anyMatch(r -> r.getId().equals(registrationId)));
    }

    @Test
    @Order(7)
    @DisplayName("按活动查询报名列表")
    void testFindByActivityId() {
        List<ActivityRegistration> list = registrationService.findByActivityId(testActivityId);
        assertFalse(list.isEmpty());
    }

    @Test
    @Order(8)
    @DisplayName("申请取消报名")
    void testRequestCancel() {
        ActivityRegistration result = registrationService.requestCancel(registrationId);
        assertEquals("CANCEL_PENDING", result.getStatus());
    }

    @Test
    @Order(9)
    @DisplayName("驳回取消申请")
    void testRejectCancel() {
        ActivityRegistration result = registrationService.rejectCancel(registrationId);
        assertEquals("REGISTERED", result.getStatus());
    }

    @Test
    @Order(10)
    @DisplayName("审批通过取消报名")
    void testApproveCancel() {
        registrationService.requestCancel(registrationId);
        ActivityRegistration result = registrationService.approveCancel(registrationId);
        assertEquals("CANCELLED", result.getStatus());
    }

    @Test
    @Order(11)
    @DisplayName("取消后重新报名（复用同一条记录）")
    void testReRegister() {
        ActivityRegistration reg = registrationService.register(testActivityId, testUserId);
        assertEquals("REGISTERED", reg.getStatus());
        assertEquals(registrationId, reg.getId(), "重新报名应复用原报名记录");
        registrationId = reg.getId();
    }

    @Test
    @Order(12)
    @DisplayName("清理 — 删除测试活动")
    void cleanup() {
        activityService.deleteById(testActivityId);
    }
}
