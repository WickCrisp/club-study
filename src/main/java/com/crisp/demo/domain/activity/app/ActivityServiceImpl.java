package com.crisp.demo.domain.activity.app;

import com.crisp.demo.domain.activity.model.Activity;
import com.crisp.demo.domain.activity.infra.ActivityMapper;
import com.crisp.demo.domain.activity.app.ActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.crisp.demo.shared.error.BusinessException;
import com.crisp.demo.shared.cache.CacheNames;

@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {

    /**
     * 活动状态机（终态不再流转）
     * DRAFT 草稿 → ONGOING 已发布/进行中 → FINISHED 已结束
     * 任意非终态均可 → CANCELLED 已取消
     */
    private static final Map<String, Set<String>> STATUS_FLOW = Map.of(
            "DRAFT",    Set.of("PENDING", "CANCELLED"),
            "PENDING",  Set.of("ONGOING", "REJECTED", "CANCELLED"),
            "REJECTED", Set.of("PENDING", "CANCELLED"),
            "ONGOING",  Set.of("FINISHED", "CANCELLED"),
            "FINISHED", Set.of(),
            "CANCELLED", Set.of()
    );

    /** 参与范围：PUBLIC 公开活动 / MEMBER_ONLY 本社团专属 */
    public static final String JOIN_SCOPE_PUBLIC = "PUBLIC";
    public static final String JOIN_SCOPE_MEMBER_ONLY = "MEMBER_ONLY";
    private static final Set<String> JOIN_SCOPES = Set.of(JOIN_SCOPE_PUBLIC, JOIN_SCOPE_MEMBER_ONLY);

    private final ActivityMapper activityMapper;
    private final com.crisp.demo.shared.event.DomainEventPublisher eventPublisher;

    @Override
    @Cacheable(value = CacheNames.ACTIVITY_DETAIL, key = "#id")
    public Activity findById(Long id) {
        Activity activity = activityMapper.findById(id);
        if (activity == null) {
            throw new BusinessException("活动不存在");
        }
        return activity;
    }

    @Override
    @Cacheable(CacheNames.ACTIVITY_LIST)
    public List<Activity> findAll() {
        return activityMapper.findAll();
    }

    @Override
    public List<Activity> findByClubId(Long clubId) {
        return activityMapper.findByClubId(clubId);
    }

    @Override
    public List<Activity> findByStatus(String status) {
        return activityMapper.findByStatus(status);
    }

    @Override
    public List<Activity> search(String keyword) {
        return activityMapper.search(keyword);
    }

    @Override
    @CacheEvict(value = CacheNames.ACTIVITY_LIST, allEntries = true)
    public Activity create(Activity activity) {
        if (activity.getStatus() == null || activity.getStatus().isEmpty()) {
            activity.setStatus("DRAFT");
        }
        if (!STATUS_FLOW.containsKey(activity.getStatus())) {
            throw new BusinessException("非法的活动状态：" + activity.getStatus());
        }
        if (activity.getJoinScope() == null || activity.getJoinScope().isEmpty()) {
            activity.setJoinScope(JOIN_SCOPE_PUBLIC);
        }
        if (!JOIN_SCOPES.contains(activity.getJoinScope())) {
            throw new BusinessException("非法的参与范围：" + activity.getJoinScope());
        }
        validateTimeRange(activity);
        activity.setCurrentCount(0);
        activityMapper.insert(activity);
        return activityMapper.findById(activity.getId());   // 回读数据库生成的创建时间等字段
    }

    @Override
    @CacheEvict(value = {CacheNames.ACTIVITY_DETAIL, CacheNames.ACTIVITY_LIST}, allEntries = true)
    public Activity update(Activity activity) {
        Activity existActivity = activityMapper.findById(activity.getId());
        if (existActivity == null) {
            throw new BusinessException("活动不存在");
        }
        validateTimeRange(activity);
        if (activity.getStatus() == null || activity.getStatus().isEmpty()) {
            activity.setStatus(existActivity.getStatus());   // 未提交状态则沿用原状态
        }
        if (activity.getJoinScope() == null || activity.getJoinScope().isEmpty()) {
            activity.setJoinScope(existActivity.getJoinScope());   // 未提交参与范围则沿用原值
        }
        if (!JOIN_SCOPES.contains(activity.getJoinScope())) {
            throw new BusinessException("非法的参与范围：" + activity.getJoinScope());
        }
        if (!existActivity.getStatus().equals(activity.getStatus())) {
            validateTransition(existActivity.getStatus(), activity.getStatus());
        }
        activityMapper.update(activity);
        return activityMapper.findById(activity.getId());
    }

    @Override
    @CacheEvict(value = {CacheNames.ACTIVITY_DETAIL, CacheNames.ACTIVITY_LIST}, allEntries = true)
    public boolean updateStatus(Long id, String status) {
        Activity existActivity = activityMapper.findById(id);
        if (existActivity == null) {
            throw new BusinessException("活动不存在");
        }
        if (!existActivity.getStatus().equals(status)) {
            validateTransition(existActivity.getStatus(), status);
        }
        return activityMapper.updateStatus(id, status) > 0;
    }

    @Override
    @CacheEvict(value = {CacheNames.ACTIVITY_DETAIL, CacheNames.ACTIVITY_LIST}, allEntries = true)
    public boolean deleteById(Long id) {
        Activity activity = activityMapper.findById(id);
        boolean deleted = activityMapper.deleteById(id) > 0;
        if (deleted && activity != null) {
            eventPublisher.publish(new com.crisp.demo.shared.event.ActivityDeleted(id, activity.getClubId()));
        }
        return deleted;
    }

    /**
     * 提交审核：草稿或被驳回的活动 → 待审核
     * 「全部活动都要审核」，因此没有任何路径能绕开审核直接变成 ONGOING
     */
    @Override
    @CacheEvict(value = {CacheNames.ACTIVITY_DETAIL, CacheNames.ACTIVITY_LIST}, allEntries = true)
    public Activity submitReview(Long id) {
        Activity activity = findById(id);
        String current = activity.getStatus();
        if (!"DRAFT".equals(current) && !"REJECTED".equals(current)) {
            throw new BusinessException("只有草稿或已驳回的活动可以提交审核，当前状态：" + current);
        }
        validateTransition(current, "PENDING");
        activityMapper.updateStatus(id, "PENDING");
        return activityMapper.findById(id);
    }

    /** 审核结果回写（由审批处理器调用） */
    @Override
    @CacheEvict(value = {CacheNames.ACTIVITY_DETAIL, CacheNames.ACTIVITY_LIST}, allEntries = true)
    public Activity review(Long id, boolean approved) {
        Activity activity = findById(id);
        if (!"PENDING".equals(activity.getStatus())) {
            throw new BusinessException("该活动不在待审核状态，当前：" + activity.getStatus());
        }
        String target = approved ? "ONGOING" : "REJECTED";
        validateTransition("PENDING", target);
        activityMapper.updateStatus(id, target);
        return activityMapper.findById(id);
    }

    /** 校验状态流转是否合法 */
    private void validateTransition(String current, String next) {
        if (!STATUS_FLOW.containsKey(next)) {
            throw new BusinessException("非法的活动状态：" + next);
        }
        Set<String> allowed = STATUS_FLOW.getOrDefault(current, Set.of());
        if (!allowed.contains(next)) {
            throw new BusinessException("不允许的状态流转：" + current + " → " + next);
        }
    }

    /** 校验时间区间 */
    private void validateTimeRange(Activity activity) {
        if (activity.getStartTime() != null && activity.getEndTime() != null
                && !activity.getEndTime().isAfter(activity.getStartTime())) {
            throw new BusinessException("结束时间必须晚于开始时间");
        }
    }
}
