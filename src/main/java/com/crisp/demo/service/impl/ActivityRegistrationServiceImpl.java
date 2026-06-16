package com.crisp.demo.service.impl;

import com.crisp.demo.entity.Activity;
import com.crisp.demo.entity.ActivityCheckin;
import com.crisp.demo.entity.ActivityRegistration;
import com.crisp.demo.mapper.ActivityCheckinMapper;
import com.crisp.demo.mapper.ActivityMapper;
import com.crisp.demo.mapper.ActivityRegistrationMapper;
import com.crisp.demo.service.ActivityRegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityRegistrationServiceImpl implements ActivityRegistrationService {

    private final ActivityRegistrationMapper registrationMapper;
    private final ActivityMapper activityMapper;
    private final ActivityCheckinMapper checkinMapper;

    @Override
    @Transactional
    public ActivityRegistration register(Long activityId, Long userId) {
        Activity activity = activityMapper.findById(activityId);
        if (activity == null) {
            throw new RuntimeException("活动不存在");
        }
        if (!"PUBLISHED".equals(activity.getStatus()) && !"ONGOING".equals(activity.getStatus())) {
            throw new RuntimeException("该活动当前不可报名");
        }
        if (activity.getMaxParticipants() > 0 && activity.getCurrentCount() >= activity.getMaxParticipants()) {
            throw new RuntimeException("报名人数已满");
        }
        ActivityRegistration existReg = registrationMapper.findByUserAndActivity(userId, activityId);
        if (existReg != null && !"CANCELLED".equals(existReg.getStatus())) {
            throw new RuntimeException("您已报名该活动");
        }
        ActivityRegistration registration = new ActivityRegistration();
        registration.setActivityId(activityId);
        registration.setUserId(userId);
        registration.setStatus("REGISTERED");
        registrationMapper.insert(registration);

        activityMapper.incrementCount(activityId);

        ActivityCheckin checkin = new ActivityCheckin();
        checkin.setRegistrationId(registration.getId());
        checkin.setActivityId(activityId);
        checkin.setUserId(userId);
        checkin.setCheckinStatus("UNCHECKED");
        checkin.setCheckoutStatus("UNCHECKED");
        checkinMapper.insert(checkin);

        return registration;
    }

    @Override
    @Transactional
    public ActivityRegistration cancel(Long registrationId) {
        ActivityRegistration registration = registrationMapper.findById(registrationId);
        if (registration == null) {
            throw new RuntimeException("报名记录不存在");
        }
        registrationMapper.cancel(registrationId);
        activityMapper.decrementCount(registration.getActivityId());
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
    public ActivityRegistration findByUserAndActivity(Long userId, Long activityId) {
        return registrationMapper.findByUserAndActivity(userId, activityId);
    }
}