package com.crisp.demo.service;

import com.crisp.demo.entity.ActivityRegistration;
import java.util.List;

public interface ActivityRegistrationService {
    ActivityRegistration register(Long activityId, Long userId);
    ActivityRegistration cancel(Long registrationId);
    ActivityRegistration requestCancel(Long registrationId);
    ActivityRegistration approveCancel(Long registrationId);
    ActivityRegistration rejectCancel(Long registrationId);
    List<ActivityRegistration> findByActivityId(Long activityId);
    List<ActivityRegistration> findByUserId(Long userId);
    List<ActivityRegistration> findByStatus(String status);
    ActivityRegistration findByUserAndActivity(Long userId, Long activityId);
}