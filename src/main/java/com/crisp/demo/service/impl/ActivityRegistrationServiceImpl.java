package com.crisp.demo.service.impl;

import com.crisp.demo.entity.Activity;
import com.crisp.demo.entity.ActivityRegistration;
import com.crisp.demo.mapper.ActivityMapper;
import com.crisp.demo.mapper.ActivityRegistrationMapper;
import com.crisp.demo.service.ActivityRegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import com.crisp.demo.common.BusinessException;

@Service
@RequiredArgsConstructor
public class ActivityRegistrationServiceImpl implements ActivityRegistrationService {

    private final ActivityRegistrationMapper registrationMapper;
    private final ActivityMapper activityMapper;

    @Override
    @Transactional
    public ActivityRegistration register(Long activityId, Long userId) {
        Activity activity = activityMapper.findById(activityId);
        if (activity == null) {
            throw new BusinessException("活动不存在");
        }
        if (!"ONGOING".equals(activity.getStatus())) {
            throw new BusinessException("该活动当前不可报名");
        }
        if (activity.getMaxParticipants() > 0 && activity.getCurrentCount() >= activity.getMaxParticipants()) {
            throw new BusinessException("报名人数已满");
        }
        ActivityRegistration existReg = registrationMapper.findByUserAndActivity(userId, activityId);
        if (existReg != null) {
            if ("REGISTERED".equals(existReg.getStatus())) {
                throw new BusinessException("您已报名该活动");
            }
            // 已取消的报名 → 重新激活，不插入新记录
            existReg.setStatus("REGISTERED");
            registrationMapper.update(existReg);
            activityMapper.incrementCount(activityId);
            return existReg;
        }
        ActivityRegistration registration = new ActivityRegistration();
        registration.setActivityId(activityId);
        registration.setUserId(userId);
        registration.setStatus("REGISTERED");
        registrationMapper.insert(registration);

        activityMapper.incrementCount(activityId);

        return registration;
    }

    @Override
    @Transactional
    public ActivityRegistration cancel(Long registrationId) {
        ActivityRegistration registration = registrationMapper.findById(registrationId);
        if (registration == null) {
            throw new BusinessException("报名记录不存在");
        }
        registrationMapper.cancel(registrationId);
        activityMapper.decrementCount(registration.getActivityId());
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
        activityMapper.decrementCount(registration.getActivityId());
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
    public ActivityRegistration findByUserAndActivity(Long userId, Long activityId) {
        return registrationMapper.findByUserAndActivity(userId, activityId);
    }
}