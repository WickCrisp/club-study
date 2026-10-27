package com.crisp.demo.domain.registration.api;

import com.crisp.demo.domain.registration.model.ActivityRegistration;

import java.util.List;

/**
 * 报名域的对外查询契约（供其他域读取报名数据，禁止直连 Mapper）
 */
public interface RegistrationQueryService {

    List<ActivityRegistration> findByActivityId(Long activityId);

    List<ActivityRegistration> findByUserId(Long userId);

    List<ActivityRegistration> findByStatus(String status);
}
