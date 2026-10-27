package com.crisp.demo.domain.activity.api;

import com.crisp.demo.domain.activity.model.Activity;

import java.util.List;

/**
 * 活动域的对外查询契约
 */
public interface ActivityQueryService {

    Activity findById(Long id);

    List<Activity> findAll();

    List<Activity> findByCreatorId(Long creatorId);

    List<Activity> findByClubId(Long clubId);

    /** 活动状态；活动不存在返回 null */
    String statusOf(Long activityId);

    /** 活动所属社团 id；活动不存在返回 null */
    Long clubIdOf(Long activityId);
}
