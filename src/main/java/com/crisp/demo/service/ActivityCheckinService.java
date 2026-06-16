package com.crisp.demo.service;

import com.crisp.demo.entity.ActivityCheckin;
import java.util.List;

public interface ActivityCheckinService {
    ActivityCheckin createCheckinRecord(Long registrationId, Long activityId, Long userId);
    ActivityCheckin checkin(Long checkinId);
    ActivityCheckin checkout(Long checkinId);
    List<ActivityCheckin> findByActivityId(Long activityId);
    List<ActivityCheckin> findByUserId(Long userId);
    ActivityCheckin findByRegistrationId(Long registrationId);
}