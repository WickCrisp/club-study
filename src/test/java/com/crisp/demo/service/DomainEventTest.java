package com.crisp.demo.service;


import com.crisp.demo.domain.activity.app.ActivityService;
import com.crisp.demo.domain.club.app.ClubService;
import com.crisp.demo.domain.identity.app.UserService;
import com.crisp.demo.domain.membership.app.ClubMemberService;
import com.crisp.demo.domain.registration.app.ActivityRegistrationService;
import com.crisp.demo.TestDemoApplication;
import com.crisp.demo.shared.event.ActivityOccupancyChanged;
import com.crisp.demo.shared.event.ClubDeleted;
import com.crisp.demo.shared.event.ClubLeaderChanged;
import com.crisp.demo.shared.event.DomainEvent;
import com.crisp.demo.domain.activity.model.Activity;
import com.crisp.demo.domain.identity.model.User;
import com.crisp.demo.support.TestFixtures;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 领域事件契约测试
 *
 * 验证「跨域变更必须广播事实」这条约定真的被触发：
 *  - 报名 → 发布 ActivityOccupancyChanged（由 activity 域自己清缓存）
 *  - 删除社团 → 发布 ClubDeleted（由受影响的域各自清缓存）
 */
@SpringBootTest(classes = {TestDemoApplication.class, DomainEventTest.RecorderConfig.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DomainEventTest {

    /** 事件记录器：只收集本项目的领域事件 */
    static class EventRecorder {
        final List<DomainEvent> events = new CopyOnWriteArrayList<>();

        @EventListener
        public void onDomainEvent(DomainEvent event) {
            events.add(event);
        }

        void clear() {
            events.clear();
        }

        boolean has(Class<? extends DomainEvent> type) {
            return events.stream().anyMatch(type::isInstance);
        }
    }

    @TestConfiguration
    static class RecorderConfig {
        @Bean
        EventRecorder eventRecorder() {
            return new EventRecorder();
        }
    }

    @Autowired private EventRecorder recorder;
    @Autowired private ActivityService activityService;
    @Autowired private ActivityRegistrationService registrationService;
    @Autowired private ClubService clubService;
    @Autowired private UserService userService;
    @Autowired private ClubMemberService clubMemberService;

    private User student;
    private Long clubId;
    private Long activityId;

    @BeforeAll
    void setUpFixtures() {
        student = TestFixtures.ensureStudent(userService);
        clubId = TestFixtures.ensureClub(clubService, student.getId()).getId();
    }

    @Test
    @Order(1)
    @DisplayName("准备一个已发布活动")
    void prepare() {
        Activity activity = new Activity();
        activity.setClubId(clubId);
        activity.setTitle("JUnit事件测试活动");
        activity.setLocation("测试地点");
        activity.setStartTime(LocalDateTime.now().plusDays(1));
        activity.setEndTime(LocalDateTime.now().plusDays(1).plusHours(2));
        activity.setMaxParticipants(10);
        activity.setCreatorId(student.getId());
        activity.setStatus("DRAFT");
        activity.setJoinScope("PUBLIC");
        activityId = activityService.create(activity).getId();
        activityService.submitReview(activityId);
        activityService.review(activityId, true);
        assertTrue(activityId > 0);
    }

    @Test
    @Order(2)
    @DisplayName("报名应发布 ActivityOccupancyChanged")
    void testRegistrationPublishesOccupancyEvent() {
        recorder.clear();
        registrationService.register(activityId, student.getId());
        assertTrue(recorder.has(ActivityOccupancyChanged.class),
                "报名后应发布 ActivityOccupancyChanged，实际事件：" + recorder.events);
    }

    @Test
    @Order(3)
    @DisplayName("取消报名也应发布 ActivityOccupancyChanged")
    void testCancelPublishesOccupancyEvent() {
        Long regId = registrationService.findByUserAndActivity(student.getId(), activityId).getId();
        recorder.clear();
        registrationService.cancel(regId);
        assertTrue(recorder.has(ActivityOccupancyChanged.class));
    }

    @Test
    @Order(4)
    @DisplayName("删除社团应发布 ClubDeleted（跨域删除告知）")
    void testClubDeletionPublishesEvent() {
        // 建一个一次性社团，避免影响夹具社团
        Long tempClubId = clubService.create(buildClub("JUnit事件临时社团")).getId();
        recorder.clear();
        clubService.deleteById(tempClubId);
        assertTrue(recorder.has(ClubDeleted.class),
                "删除社团应发布 ClubDeleted，实际事件：" + recorder.events);
    }

    @Test
    @Order(5)
    @DisplayName("创建社团应发布 ClubLeaderChanged，并由 membership 域同步出 LEADER 成员记录")
    void testClubCreationSyncsLeaderMember() {
        recorder.clear();
        Long newClubId = clubService.create(buildClub("JUnit负责人同步社团")).getId();

        assertTrue(recorder.has(ClubLeaderChanged.class),
                "创建社团应发布 ClubLeaderChanged，实际事件：" + recorder.events);

        var member = clubMemberService.findByUserAndClub(student.getId(), newClubId);
        assertNotNull(member, "membership 域应通过事件创建成员记录");
        assertEquals("LEADER", member.getRole());
        assertEquals("APPROVED", member.getStatus());

        clubService.deleteById(newClubId);   // 级联删除成员记录
    }

    @Test
    @Order(6)
    @DisplayName("清理")
    void cleanup() {
        activityService.deleteById(activityId);
    }

    private com.crisp.demo.domain.club.model.Club buildClub(String name) {
        com.crisp.demo.domain.club.model.Club club = new com.crisp.demo.domain.club.model.Club();
        club.setName(name);
        club.setDescription("事件测试");
        club.setLeaderId(student.getId());
        club.setStatus("ACTIVE");
        return club;
    }
}
