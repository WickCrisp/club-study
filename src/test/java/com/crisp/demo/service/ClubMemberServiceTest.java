package com.crisp.demo.service;


import com.crisp.demo.domain.activity.app.ActivityService;
import com.crisp.demo.domain.club.app.ClubService;
import com.crisp.demo.domain.identity.app.UserService;
import com.crisp.demo.domain.membership.app.ClubMemberService;
import com.crisp.demo.domain.registration.app.ActivityRegistrationService;
import com.crisp.demo.TestDemoApplication;
import com.crisp.demo.domain.membership.model.ClubMember;
import com.crisp.demo.support.TestFixtures;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 社团成员模块测试 — 申请、审批、退出、重新申请
 * 测试数据由 TestFixtures 现场准备，不依赖数据库里的固定 id
 */
@SpringBootTest(classes = TestDemoApplication.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ClubMemberServiceTest {

    @Autowired
    private ClubMemberService clubMemberService;

    @Autowired
    private UserService userService;

    @Autowired
    private ClubService clubService;

    private Long testUserId;
    private Long testClubId;
    private static Long memberId;

    @BeforeAll
    void setUpFixtures() {
        testUserId = TestFixtures.ensureStudent(userService).getId();
        testClubId = TestFixtures.ensureClub(clubService, testUserId).getId();
        // 清理上次失败运行可能残留的成员记录
        ClubMember exist = clubMemberService.findByUserAndClub(testUserId, testClubId);
        if (exist != null) {
            clubMemberService.deleteById(exist.getId());
        }
    }

    @Test
    @Order(1)
    @DisplayName("申请加入社团")
    void testApply() {
        ClubMember member = clubMemberService.apply(testUserId, testClubId, "我想加入");
        assertNotNull(member.getId());
        assertEquals("PENDING", member.getStatus());
        assertEquals("MEMBER", member.getRole());
        memberId = member.getId();
    }

    @Test
    @Order(2)
    @DisplayName("重复申请应失败")
    void testDuplicateApply() {
        assertThrows(RuntimeException.class,
                () -> clubMemberService.apply(testUserId, testClubId, "再次申请"),
                "重复申请应抛出异常");
    }

    @Test
    @Order(3)
    @DisplayName("待审批列表包含该申请")
    void testFindPending() {
        var pending = clubMemberService.findPendingByClubId(testClubId);
        assertFalse(pending.isEmpty());
        assertTrue(pending.stream().anyMatch(m -> m.getId().equals(memberId)));
    }

    @Test
    @Order(4)
    @DisplayName("审批通过")
    void testApprove() {
        ClubMember result = clubMemberService.approve(memberId);
        assertEquals("APPROVED", result.getStatus());
        assertNotNull(result.getJoinedAt());
    }

    @Test
    @Order(5)
    @DisplayName("已是成员再次申请应失败")
    void testApplyAsMember() {
        assertThrows(RuntimeException.class,
                () -> clubMemberService.apply(testUserId, testClubId, "再申请一次"));
    }

    @Test
    @Order(6)
    @DisplayName("查询用户的社团列表")
    void testFindByUserId() {
        var members = clubMemberService.findByUserId(testUserId);
        assertFalse(members.isEmpty());
    }

    @Test
    @Order(7)
    @DisplayName("查询社团成员列表")
    void testFindByClubId() {
        var members = clubMemberService.findByClubId(testClubId);
        assertFalse(members.isEmpty());
    }

    @Test
    @Order(8)
    @DisplayName("申请退出社团")
    void testRequestQuit() {
        ClubMember result = clubMemberService.requestQuit(memberId);
        assertEquals("QUIT_PENDING", result.getStatus());
    }

    @Test
    @Order(9)
    @DisplayName("非APPROVED状态不可申请退出")
    void testRequestQuitInvalidStatus() {
        assertThrows(RuntimeException.class,
                () -> clubMemberService.requestQuit(memberId),
                "QUIT_PENDING 状态不可再次申请退出");
    }

    @Test
    @Order(10)
    @DisplayName("审批退出通过")
    void testApproveQuit() {
        ClubMember result = clubMemberService.approveQuit(memberId);
        assertEquals("QUIT", result.getStatus());
    }

    @Test
    @Order(11)
    @DisplayName("退出后重新申请")
    void testReapplyAfterQuit() {
        ClubMember member = clubMemberService.apply(testUserId, testClubId, "重新回来");
        assertEquals("PENDING", member.getStatus());
        memberId = member.getId();
    }

    @Test
    @Order(12)
    @DisplayName("拒绝申请")
    void testReject() {
        ClubMember result = clubMemberService.reject(memberId);
        assertEquals("REJECTED", result.getStatus());
    }

    @Test
    @Order(13)
    @DisplayName("被拒绝后重新申请（复用同一条记录）")
    void testReapplyAfterReject() {
        Long beforeId = memberId;
        ClubMember member = clubMemberService.apply(testUserId, testClubId, "再试试");
        assertEquals("PENDING", member.getStatus());
        assertEquals(beforeId, member.getId(), "(user_id, club_id) 唯一约束下应复用同一条记录");
        memberId = member.getId();
    }

    @Test
    @Order(14)
    @DisplayName("清理 — 删除成员记录")
    void testDelete() {
        boolean result = clubMemberService.deleteById(memberId);
        assertTrue(result);
    }
}
