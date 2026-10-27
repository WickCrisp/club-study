package com.crisp.demo.domain.registration.app;

import com.crisp.demo.domain.activity.model.Activity;
import com.crisp.demo.domain.registration.model.ActivityRegistration;
import com.crisp.demo.domain.activity.api.ActivityOccupancyPort;
import com.crisp.demo.domain.activity.api.ActivityQueryService;
import com.crisp.demo.domain.registration.infra.ActivityRegistrationMapper;
import com.crisp.demo.domain.registration.api.RegistrationQueryService;
import com.crisp.demo.domain.registration.app.ActivityRegistrationService;
import com.crisp.demo.domain.membership.api.MembershipQueryService;
import com.crisp.demo.shared.event.ActivityOccupancyChanged;
import com.crisp.demo.shared.event.RegistrationCancelled;
import com.crisp.demo.shared.event.DomainEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import com.crisp.demo.shared.error.BusinessException;

@Service
@RequiredArgsConstructor
public class ActivityRegistrationServiceImpl implements ActivityRegistrationService, RegistrationQueryService {

    private final ActivityRegistrationMapper registrationMapper;
    private final ActivityQueryService activityQueryService;
    private final ActivityOccupancyPort activityOccupancyPort;
    private final MembershipQueryService membershipQueryService;
    private final DomainEventPublisher eventPublisher;

    @Override
    public ActivityRegistration findById(Long id) {
        ActivityRegistration registration = registrationMapper.findById(id);
        if (registration == null) {
            throw new BusinessException("报名记录不存在");
        }
        return registration;
    }

    /**
     * 报名活动
     *
     * 名额控制依赖数据库的原子更新：
     *   UPDATE activity SET current_count = current_count + 1
     *   WHERE id = ? AND (max_participants = 0 OR current_count < max_participants)
     * 只有受影响行数为 1 的请求才真正抢到名额，行数为 0 说明已满，直接抛异常回滚。
     * 先占位再落库，避免"先查后插"在并发下超卖。
     */
    @Override
    @Transactional
    public ActivityRegistration register(Long activityId, Long userId) {
        Activity activity = activityQueryService.findById(activityId);
        if (activity == null) {
            throw new BusinessException("活动不存在");
        }
        if (!"ONGOING".equals(activity.getStatus())) {
            throw new BusinessException("该活动当前不可报名（仅已发布/进行中的活动可报名）");
        }

        // 本社团专属活动：仅该社团 APPROVED 成员可报名；公开活动不限
        if ("MEMBER_ONLY".equals(activity.getJoinScope())
                && !membershipQueryService.isApproved(activity.getClubId(), userId)) {
            throw new BusinessException("该活动仅限本社团成员报名，请先加入社团并通过审核");
        }

        ActivityRegistration existReg = registrationMapper.findByUserAndActivity(userId, activityId);
        if (existReg != null) {
            if ("REGISTERED".equals(existReg.getStatus())) {
                throw new BusinessException("您已报名该活动");
            }
            if ("CANCEL_PENDING".equals(existReg.getStatus())) {
                throw new BusinessException("您已提交取消申请，请等待审批结果");
            }
        }

        // 原子占位：抢不到名额直接失败，事务回滚
        if (!activityOccupancyPort.occupy(activityId)) {
            throw new BusinessException("报名人数已满");
        }

        if (existReg != null) {
            // 之前取消过的报名 → 复用旧记录，不插入新记录
            existReg.setStatus("REGISTERED");
            existReg.setCancelledAt(null);
            registrationMapper.update(existReg);
            eventPublisher.publish(new ActivityOccupancyChanged(activityId));
            return registrationMapper.findById(existReg.getId());
        }

        ActivityRegistration registration = new ActivityRegistration();
        registration.setActivityId(activityId);
        registration.setUserId(userId);
        registration.setStatus("REGISTERED");
        registrationMapper.insert(registration);
        eventPublisher.publish(new ActivityOccupancyChanged(activityId));   // 通知 activity 域清自己的缓存
        return registrationMapper.findById(registration.getId());
    }

    /** 直接取消报名 — 仅已报名/待审批取消的记录可取消，避免重复回退人数 */
    @Override
    @Transactional
    public ActivityRegistration cancel(Long registrationId) {
        ActivityRegistration registration = registrationMapper.findById(registrationId);
        if (registration == null) {
            throw new BusinessException("报名记录不存在");
        }
        if ("CANCELLED".equals(registration.getStatus())) {
            throw new BusinessException("该报名已取消，请勿重复操作");
        }
        registrationMapper.cancel(registrationId);
        activityOccupancyPort.release(registration.getActivityId());
        eventPublisher.publish(new ActivityOccupancyChanged(registration.getActivityId()));
        return registrationMapper.findById(registrationId);
    }

    @Override
    @Transactional
    public ActivityRegistration requestCancel(Long registrationId) {
        ActivityRegistration registration = registrationMapper.findById(registrationId);
        if (registration == null) {
            throw new BusinessException("报名记录不存在");
        }
        if (!"REGISTERED".equals(registration.getStatus())) {
            throw new BusinessException("当前状态不可申请取消");
        }
        registrationMapper.requestCancel(registrationId);
        return registrationMapper.findById(registrationId);
    }

    @Override
    @Transactional
    public ActivityRegistration approveCancel(Long registrationId) {
        ActivityRegistration registration = registrationMapper.findById(registrationId);
        if (registration == null) {
            throw new BusinessException("报名记录不存在");
        }
        if (!"CANCEL_PENDING".equals(registration.getStatus())) {
            throw new BusinessException("该报名未申请取消");
        }
        registrationMapper.approveCancel(registrationId);
        activityOccupancyPort.release(registration.getActivityId());
        eventPublisher.publish(new ActivityOccupancyChanged(registration.getActivityId()));
        return registrationMapper.findById(registrationId);
    }

    @Override
    @Transactional
    public ActivityRegistration rejectCancel(Long registrationId) {
        ActivityRegistration registration = registrationMapper.findById(registrationId);
        if (registration == null) {
            throw new BusinessException("报名记录不存在");
        }
        if (!"CANCEL_PENDING".equals(registration.getStatus())) {
            throw new BusinessException("该报名未申请取消");
        }
        registrationMapper.rejectCancel(registrationId);
        return registrationMapper.findById(registrationId);
    }

    @Override
    public List<ActivityRegistration> findByActivityId(Long activityId) {
        return registrationMapper.findByActivityId(activityId);
    }

    @Override
    public List<ActivityRegistration> findByUserId(Long userId) {
        return registrationMapper.findByUserId(userId);
    }

    @Override
    public List<ActivityRegistration> findByStatus(String status) {
        return registrationMapper.findByStatus(status);
    }

    @Override
    public List<ActivityRegistration> findAllDetail(String status) {
        return registrationMapper.findAllDetail(status == null || status.isBlank() ? null : status);
    }
    @Override
    public ActivityRegistration findByUserAndActivity(Long userId, Long activityId) {
        return registrationMapper.findByUserAndActivity(userId, activityId);
    }
}
