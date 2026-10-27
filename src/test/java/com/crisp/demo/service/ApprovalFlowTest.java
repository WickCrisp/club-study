package com.crisp.demo.service;

import com.crisp.demo.TestDemoApplication;
import com.crisp.demo.domain.activity.app.ActivityService;
import com.crisp.demo.domain.activity.model.Activity;
import com.crisp.demo.domain.approval.api.ApprovalCommandService;
import com.crisp.demo.domain.approval.api.ApprovalQueryService;
import com.crisp.demo.domain.approval.model.ApprovalTask;
import com.crisp.demo.domain.approval.model.ApprovalType;
import com.crisp.demo.domain.club.app.ClubService;
import com.crisp.demo.domain.club.model.Club;
import com.crisp.demo.domain.identity.app.UserService;
import com.crisp.demo.domain.identity.model.User;
import com.crisp.demo.domain.membership.app.ClubMemberService;
import com.crisp.demo.domain.membership.model.ClubMember;
import com.crisp.demo.domain.registration.app.ActivityRegistrationService;
import com.crisp.demo.domain.registration.model.ActivityRegistration;
import com.crisp.demo.support.TestFixtures;
import com.crisp.demo.shared.error.ForbiddenException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 待办中心测试 — 五类审批的决策会正确回写到目标域
 *
 * 验证「待办中心只记录与决策，状态变更由目标域执行」这条边界。
 */
@SpringBootTest(classes = TestDemoApplication.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ApprovalFlowTest {

    @Autowired private ApprovalCommandService approvalCommandService;
    @Autowired private ApprovalQueryService approvalQueryService;
    @Autowired private com.crisp.demo.domain.approval.app.ApprovalServiceImpl approvalService;
    @Autowired private ClubService clubService;
    @Autowired private ActivityService activityService;
    @Autowired private ClubMemberService clubMemberService;
    @Autowired private ActivityRegistrationService registrationService;
    @Autowired private UserService userService;

    private User student;     // 申请人
    private User another;     // 另一个用户（审批人）
    private User admin;

    @BeforeAll
    void setUpFixtures() {
        student = TestFixtures.ensureUser(userService, "junit0201", "JUnit审批申请人", "STUDENT");
        another = TestFixtures.ensureUser(userService, "junit0202", "JUnit审批人", "STUDENT");
        admin = TestFixtures.ensureUser(userService, "junitadmin", "JUnit管理员", "ADMIN");
    }

    @Test
    @Order(1)
    @DisplayName("社团创建审批：PENDING_REVIEW → 通过后 ACTIVE")
    void testClubCreationApproval() {
        Club club = new Club();
        club.setName("JUnit审批社团A");
        club.setDescription("待审核");
        club.setLeaderId(student.getId());
        club.setStatus("PENDING_REVIEW");
        Club saved = clubService.create(club);
        assertEquals("PENDING_REVIEW", saved.getStatus());

        approvalCommandService.createTask(ApprovalType.CLUB_CREATE, "CLUB", saved.getId(), saved.getId(),
                student.getId(), ApprovalTask.SCOPE_ADMIN, null, "社团创建申请：" + saved.getName(), null);

        ApprovalTask task = approvalQueryService.findPendingByTarget(ApprovalType.CLUB_CREATE, saved.getId());
        assertNotNull(task, "应生成待办");
        assertEquals(1, approvalQueryService.pendingCount(admin.getId(), true), "管理员应有 1 条待办");

        approvalCommandService.approve(task.getId(), admin.getId(), "资料齐全");

        assertEquals("ACTIVE", clubService.findById(saved.getId()).getStatus(), "审批通过后社团应生效");
        clubService.deleteById(saved.getId());
    }

    @Test
    @Order(2)
    @DisplayName("社团创建审批：驳回后 REJECTED")
    void testClubCreationRejected() {
        Club club = new Club();
        club.setName("JUnit审批社团B");
        club.setLeaderId(student.getId());
        club.setStatus("PENDING_REVIEW");
        Club saved = clubService.create(club);

        approvalCommandService.createTask(ApprovalType.CLUB_CREATE, "CLUB", saved.getId(), saved.getId(),
                student.getId(), ApprovalTask.SCOPE_ADMIN, null, "社团创建申请：" + saved.getName(), null);
        ApprovalTask task = approvalQueryService.findPendingByTarget(ApprovalType.CLUB_CREATE, saved.getId());

        approvalCommandService.reject(task.getId(), admin.getId(), "名称不规范");
        assertEquals("REJECTED", clubService.findById(saved.getId()).getStatus());
        clubService.deleteById(saved.getId());
    }

    @Test
    @Order(3)
    @DisplayName("活动审核：提交后 PENDING，通过后 ONGOING")
    void testActivityReviewApproval() {
        Club club = createActiveClub("JUnit审批社团C");
        Activity activity = new Activity();
        activity.setClubId(club.getId());
        activity.setTitle("JUnit审核活动");
        activity.setLocation("测试地点");
        activity.setStartTime(LocalDateTime.now().plusDays(1));
        activity.setEndTime(LocalDateTime.now().plusDays(1).plusHours(2));
        activity.setMaxParticipants(10);
        activity.setCreatorId(student.getId());
        activity.setStatus("DRAFT");
        activity.setJoinScope("PUBLIC");
        Long activityId = activityService.create(activity).getId();

        activityService.submitReview(activityId);
        assertEquals("PENDING", activityService.findById(activityId).getStatus());

        approvalCommandService.createTask(ApprovalType.ACTIVITY_REVIEW, "ACTIVITY", activityId, club.getId(),
                student.getId(), ApprovalTask.SCOPE_ADMIN, null, "活动审核：" + activity.getTitle(), null);
        ApprovalTask task = approvalQueryService.findPendingByTarget(ApprovalType.ACTIVITY_REVIEW, activityId);

        approvalCommandService.approve(task.getId(), admin.getId(), "同意");
        assertEquals("ONGOING", activityService.findById(activityId).getStatus(), "审核通过后活动可报名");

        activityService.deleteById(activityId);
        clubService.deleteById(club.getId());
    }

    @Test
    @Order(4)
    @DisplayName("入社审批：须由指定审批人处理，越权被拒")
    void testJoinApprovalAndPermission() {
        Club club = createActiveClub("JUnit审批社团D");
        ClubMember applied = clubMemberService.apply(student.getId(), club.getId(), "想加入");

        approvalCommandService.createTask(ApprovalType.JOIN, "CLUB_MEMBER", applied.getId(), club.getId(),
                student.getId(), ApprovalTask.SCOPE_USER, another.getId(), "入社申请", null);

        ApprovalTask task = approvalQueryService.findPendingByTarget(ApprovalType.JOIN, applied.getId());
        assertNotNull(task);
        assertEquals(1, approvalQueryService.pendingCount(another.getId(), false), "指定审批人应看到 1 条待办");

        // 非指定审批人操作 → 拒绝
        assertThrows(ForbiddenException.class,
                () -> approvalService.requireDecidable(task, student.getId(), false));

        approvalService.requireDecidable(task, another.getId(), false);   // 指定审批人可处理

        approvalCommandService.approve(task.getId(), another.getId(), "欢迎");
        assertEquals("APPROVED", clubMemberService.findById(applied.getId()).getStatus());

        clubService.deleteById(club.getId());
    }

    @Test
    @Order(5)
    @DisplayName("取消报名审批：通过后名额回退")
    void testCancelRegistrationApproval() {
        Club club = createActiveClub("JUnit审批社团E");
        Activity activity = new Activity();
        activity.setClubId(club.getId());
        activity.setTitle("JUnit取消审批活动");
        activity.setLocation("测试地点");
        activity.setStartTime(LocalDateTime.now().plusDays(1));
        activity.setEndTime(LocalDateTime.now().plusDays(1).plusHours(2));
        activity.setMaxParticipants(10);
        activity.setCreatorId(another.getId());
        activity.setStatus("DRAFT");
        activity.setJoinScope("PUBLIC");
        Long activityId = activityService.create(activity).getId();
        activityService.submitReview(activityId);
        activityService.review(activityId, true);

        ActivityRegistration reg = registrationService.register(activityId, student.getId());
        registrationService.requestCancel(reg.getId());

        approvalCommandService.createTask(ApprovalType.CANCEL_REGISTRATION, "ACTIVITY_REGISTRATION",
                reg.getId(), club.getId(), student.getId(), ApprovalTask.SCOPE_USER, another.getId(),
                "取消报名申请", null);
        ApprovalTask task = approvalQueryService.findPendingByTarget(ApprovalType.CANCEL_REGISTRATION, reg.getId());

        approvalCommandService.approve(task.getId(), another.getId(), "同意取消");

        assertEquals("CANCELLED", registrationService.findById(reg.getId()).getStatus());
        assertEquals(0, activityService.findById(activityId).getCurrentCount(), "名额应回退");

        activityService.deleteById(activityId);
        clubService.deleteById(club.getId());
    }

    @Test
    @Order(6)
    @DisplayName("同一待办不能重复决策")
    void testDuplicateDecisionRejected() {
        Club club = createActiveClub("JUnit审批社团F");
        ClubMember applied = clubMemberService.apply(student.getId(), club.getId(), "再次申请");

        approvalCommandService.createTask(ApprovalType.JOIN, "CLUB_MEMBER", applied.getId(), club.getId(),
                student.getId(), ApprovalTask.SCOPE_USER, another.getId(), "入社申请", null);
        ApprovalTask task = approvalQueryService.findPendingByTarget(ApprovalType.JOIN, applied.getId());

        approvalCommandService.approve(task.getId(), another.getId(), "通过");
        assertThrows(RuntimeException.class,
                () -> approvalCommandService.approve(task.getId(), another.getId(), "重复通过"),
                "已处理的待办不能再次决策");

        clubService.deleteById(club.getId());
    }

    @Test
    @Order(7)
    @DisplayName("我发起的申请可查询")
    void testMyApplications() {
        List<ApprovalTask> mine = approvalQueryService.myApplications(student.getId());
        assertFalse(mine.isEmpty(), "申请人应能看到自己发起的审批");
    }

    private Club createActiveClub(String name) {
        Club club = new Club();
        club.setName(name);
        club.setDescription("审批测试");
        club.setLeaderId(another.getId());
        club.setStatus("ACTIVE");
        return clubService.create(club);
    }
}
