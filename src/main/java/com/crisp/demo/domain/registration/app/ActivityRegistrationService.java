package com.crisp.demo.domain.registration.app;

import com.crisp.demo.domain.registration.model.ActivityRegistration;
import java.util.List;

public interface ActivityRegistrationService {
    ActivityRegistration findById(Long id);
    ActivityRegistration register(Long activityId, Long userId);
    ActivityRegistration cancel(Long registrationId);
    ActivityRegistration requestCancel(Long registrationId);
    ActivityRegistration approveCancel(Long registrationId);
    ActivityRegistration rejectCancel(Long registrationId);
    List<ActivityRegistration> findByActivityId(Long activityId);
    List<ActivityRegistration> findByUserId(Long userId);
    List<ActivityRegistration> findByStatus(String status);
    ActivityRegistration findByUserAndActivity(Long userId, Long activityId);
    /** 全量报名（带活动/社团信息），status 为空表示全部 */
    List<ActivityRegistration> findAllDetail(String status);
}