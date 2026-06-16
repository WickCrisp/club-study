package com.crisp.demo.service.impl;

import com.crisp.demo.entity.ActivityCheckin;
import com.crisp.demo.mapper.ActivityCheckinMapper;
import com.crisp.demo.service.ActivityCheckinService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityCheckinServiceImpl implements ActivityCheckinService {

    private final ActivityCheckinMapper checkinMapper;

    @Override
    public ActivityCheckin createCheckinRecord(Long registrationId, Long activityId, Long userId) {
        ActivityCheckin checkin = new ActivityCheckin();
        checkin.setRegistrationId(registrationId);
        checkin.setActivityId(activityId);
        checkin.setUserId(userId);
        checkin.setCheckinStatus("UNCHECKED");
        checkin.setCheckoutStatus("UNCHECKED");
        checkinMapper.insert(checkin);
        return checkin;
    }

    @Override
    public ActivityCheckin checkin(Long checkinId) {
        ActivityCheckin checkin = checkinMapper.findById(checkinId);
        if (checkin == null) {
            throw new RuntimeException("签到记录不存在");
        }
        if ("CHECKED".equals(checkin.getCheckinStatus())) {
            throw new RuntimeException("已签到，请勿重复签到");
        }
        checkinMapper.checkin(checkinId);
        return checkinMapper.findById(checkinId);
    }

    @Override
    public ActivityCheckin checkout(Long checkinId) {
        ActivityCheckin checkin = checkinMapper.findById(checkinId);
        if (checkin == null) {
            throw new RuntimeException("签退记录不存在");
        }
        if ("CHECKED".equals(checkin.getCheckoutStatus())) {
            throw new RuntimeException("已签退，请勿重复签退");
        }
        checkinMapper.checkout(checkinId);
        return checkinMapper.findById(checkinId);
    }

    @Override
    public List<ActivityCheckin> findByActivityId(Long activityId) {
        return checkinMapper.findByActivityId(activityId);
    }

    @Override
    public List<ActivityCheckin> findByUserId(Long userId) {
        return checkinMapper.findByUserId(userId);
    }

    @Override
    public ActivityCheckin findByRegistrationId(Long registrationId) {
        return checkinMapper.findByRegistrationId(registrationId);
    }
}