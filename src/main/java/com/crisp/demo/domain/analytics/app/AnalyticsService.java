package com.crisp.demo.domain.analytics.app;

import com.crisp.demo.domain.activity.api.ActivityQueryService;
import com.crisp.demo.domain.activity.model.Activity;
import com.crisp.demo.domain.announcement.app.AnnouncementService;
import com.crisp.demo.domain.approval.api.ApprovalQueryService;
import com.crisp.demo.domain.club.api.ClubQueryService;
import com.crisp.demo.domain.identity.app.UserService;
import com.crisp.demo.domain.membership.app.ClubMemberService;
import com.crisp.demo.domain.membership.model.ClubMember;
import com.crisp.demo.domain.registration.api.RegistrationQueryService;
import com.crisp.demo.domain.registration.model.ActivityRegistration;
import com.crisp.demo.domain.showcase.app.ClubShowcaseService;
import com.crisp.demo.shared.error.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 统计域 —— 只读聚合，不持有任何业务写权限
 *
 * 数据来源全部是各域的查询契约，不直接访问其他域的表。
 */
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final UserService userService;
    private final ClubQueryService clubQueryService;
    private final ClubMemberService clubMemberService;
    private final ActivityQueryService activityQueryService;
    private final RegistrationQueryService registrationQueryService;
    private final ClubShowcaseService showcaseService;
    private final AnnouncementService announcementService;
    private final ApprovalQueryService approvalQueryService;

    /** 全局统计（管理员） */
    public Map<String, Object> global() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("users", userService.findAll().size());
        data.put("clubs", clubQueryService.findAll().size());
        data.put("activities", activityQueryService.findAll().size());
        data.put("announcements", announcementService.countPublished());
        data.put("showcases", showcaseService.countPublished());
        data.put("pendingApprovals", approvalQueryService.pendingCountAll());
        return data;
    }

    /** 社团统计（负责人 / 管理员） */
    public Map<String, Object> club(Long clubId) {
        if (clubQueryService.findById(clubId) == null) {
            throw new BusinessException("社团不存在");
        }
        List<ClubMember> members = clubMemberService.findByClubId(clubId);
        long approvedMembers = members.stream().filter(m -> "APPROVED".equals(m.getStatus())).count();
        int pendingMembers = clubMemberService.findPendingByClubId(clubId).size()
                + clubMemberService.findQuitPendingByClubId(clubId).size();

        List<Activity> activities = activityQueryService.findByClubId(clubId);
        long ongoing = activities.stream().filter(a -> "ONGOING".equals(a.getStatus())).count();
        long registrations = 0;
        for (Activity activity : activities) {
            registrations += registrationQueryService.findByActivityId(activity.getId()).stream()
                    .filter(r -> "REGISTERED".equals(r.getStatus())).count();
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("clubId", clubId);
        data.put("members", approvedMembers);
        data.put("pendingMembers", pendingMembers);
        data.put("activities", activities.size());
        data.put("ongoingActivities", ongoing);
        data.put("registrations", registrations);
        data.put("showcases", showcaseService.countPublishedByClub(clubId));
        return data;
    }
}
