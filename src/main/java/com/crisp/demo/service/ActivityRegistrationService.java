package com.crisp.demo.service;

import com.crisp.demo.entity.ActivityRegistration;
import java.util.List;

public interface ActivityRegistrationService {
    ActivityRegistration register(Long activityId, Long userId);
    ActivityRegistration cancel(Long registrationId);
    List<ActivityRegistration> findByActivityId(Long activityId);
    List<ActivityRegistration> findByUserId(Long userId);
    ActivityRegistration findByUserAndActivity(Long userId, Long activityId);
}