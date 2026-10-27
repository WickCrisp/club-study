package com.crisp.demo.service;


import com.crisp.demo.domain.activity.app.ActivityService;
import com.crisp.demo.domain.club.app.ClubService;
import com.crisp.demo.domain.identity.app.UserService;
import com.crisp.demo.domain.membership.app.ClubMemberService;
import com.crisp.demo.domain.registration.app.ActivityRegistrationService;
import com.crisp.demo.TestDemoApplication;
import com.crisp.demo.domain.activity.model.Activity;
import com.crisp.demo.domain.registration.model.ActivityRegistration;
import com.crisp.demo.domain.membership.model.ClubMember;
import com.crisp.demo.domain.identity.model.User;
import com.crisp.demo.support.TestFixtures;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 阶段 1 规则测试：
 *   1) 参与范围（PUBLIC / MEMBER_ONLY）对报名资格的约束
 *   2) 取消报名申请的审批闭环
 *   3) 名额口径：REGISTERED + CANCEL_PENDING 都占名额
 *
 * 注：签到签退与活动总结模块已按功能树下线，相关断言同步移除。
 */
@SpringBootTest(classes = TestDemoApplication.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ActivityRegistrationRuleTest {

    @Autowired private ActivityRegistrationService registrationService;
    @Autowired private ActivityService activityService;
    @Autowired private ClubMemberService clubMemberService;
    @Autowired private ClubService clubService;
    @Autowired private UserService userService;

    private User member;        // 加入社团并通过审核的成员
    private User outsider;      // 未加入社团的用户
    private Long clubId;
    private static Long memberOnlyActivityId;
    private static Long publicActivityId;
    private static Long registrationId;

    @BeforeAll
    void setUpFixtures() {
        member = TestFixtures.ensureUser(userService, "junit0101", "JUnit规则成员", "STUDENT");
        outsider = TestFixtures.ensureUser(userService, "junit0102", "JUnit规则外部用户", "STUDENT");
        clubId = TestFixtures.ensureClub(clubService, member.getId()).getId();

        ClubMember exist = clubMemberService.findByUserAndClub(member.getId(), clubId);
        if (exist != null) {
            clubMemberService.deleteById(exist.getId());
        }
        ClubMember applied = clubMemberService.apply(member.getId(), clubId, "规则测试");
        clubMemberService.approve(applied.getId());
    }

    @Test
    @Order(1)
    @DisplayName("创建两个进行中的活动：一个本社团专属，一个公开")
    void createActivities() {
        memberOnlyActivityId = activityService.create(
                buildActivity("JUnit社团专属活动", "MEMBER_ONLY")).getId();
        publicActivityId = activityService.create(
                buildActivity("JUnit公开活动", "PUBLIC")).getId();
        activityService.submitReview(memberOnlyActivityId);
        activityService.review(memberOnlyActivityId, true);
        activityService.submitReview(publicActivityId);
        activityService.review(publicActivityId, true);
        assertTrue(memberOnlyActivityId > 0 && publicActivityId > 0);
    }

    @Test
    @Order(2)
    @DisplayName("本社团专属活动：非成员报名被拒")
    void testMemberOnlyRejectedForOutsider() {
        RuntimeException e = assertThrows(RuntimeException.class,
                () -> registrationService.register(memberOnlyActivityId, outsider.getId()));
        assertTrue(e.getMessage().contains("仅限本社团成员"), "错误信息应说明成员限制，实际：" + e.getMessage());
    }

    @Test
    @Order(3)
    @DisplayName("本社团专属活动：成员报名成功")
    void testMemberOnlyAllowedForMember() {
        ActivityRegistration reg = registrationService.register(memberOnlyActivityId, member.getId());
        assertEquals("REGISTERED", reg.getStatus());
        registrationId = reg.getId();
    }

    @Test
    @Order(4)
    @DisplayName("公开活动：非成员也能报名")
    void testPublicAllowedForOutsider() {
        ActivityRegistration reg = registrationService.register(publicActivityId, outsider.getId());
        assertEquals("REGISTERED", reg.getStatus());
        assertNotNull(reg.getId());
    }

    @Test
    @Order(5)
    @DisplayName("申请取消：名额仍被占用（CANCEL_PENDING 占位口径）")
    void testCancelPendingStillOccupiesSeat() {
        registrationService.requestCancel(registrationId);
        Activity activity = activityService.findById(memberOnlyActivityId);
        assertEquals(1, activity.getCurrentCount(), "待审批取消期间名额仍占用");
    }

    @Test
    @Order(6)
    @DisplayName("审批通过 → 状态取消、名额回退")
    void testApproveCancelFlow() {
        ActivityRegistration cancelled = registrationService.approveCancel(registrationId);
        assertEquals("CANCELLED", cancelled.getStatus());

        Activity activity = activityService.findById(memberOnlyActivityId);
        assertEquals(0, activity.getCurrentCount(), "取消后应回退名额");
    }

    @Test
    @Order(7)
    @DisplayName("重新报名：复用报名记录并重新占位")
    void testReRegister() {
        ActivityRegistration reg = registrationService.register(memberOnlyActivityId, member.getId());
        assertEquals(registrationId, reg.getId(), "应复用原报名记录");
        registrationId = reg.getId();
        assertEquals(1, activityService.findById(memberOnlyActivityId).getCurrentCount());
    }

    @Test
    @Order(8)
    @DisplayName("申请取消 → 驳回 → 仍是已报名且名额不变")
    void testRejectCancelFlow() {
        registrationService.requestCancel(registrationId);
        ActivityRegistration reg = registrationService.rejectCancel(registrationId);
        assertEquals("REGISTERED", reg.getStatus());
        assertEquals(1, activityService.findById(memberOnlyActivityId).getCurrentCount(), "驳回不应回退名额");
    }

    @Test
    @Order(9)
    @DisplayName("清理测试活动")
    void cleanup() {
        activityService.deleteById(memberOnlyActivityId);
        activityService.deleteById(publicActivityId);
        assertThrows(RuntimeException.class,
                () -> activityService.findById(memberOnlyActivityId));
    }

    private Activity buildActivity(String title, String joinScope) {
        Activity activity = new Activity();
        activity.setClubId(clubId);
        activity.setTitle(title);
        activity.setDescription("规则测试用");
        activity.setLocation("测试地点");
        activity.setStartTime(LocalDateTime.now().plusDays(1));
        activity.setEndTime(LocalDateTime.now().plusDays(1).plusHours(2));
        activity.setMaxParticipants(10);
        activity.setCreatorId(member.getId());
        activity.setStatus("DRAFT");
        activity.setJoinScope(joinScope);
        return activity;
    }
}
