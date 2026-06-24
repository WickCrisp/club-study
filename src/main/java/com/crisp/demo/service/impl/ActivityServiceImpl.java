package com.crisp.demo.service.impl;

import com.crisp.demo.entity.Activity;
import com.crisp.demo.mapper.ActivityMapper;
import com.crisp.demo.service.ActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

import com.crisp.demo.common.BusinessException;

@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {

    private final ActivityMapper activityMapper;

    @Override
    @Cacheable(value = "activity", key = "#id")
    public Activity findById(Long id) {
        Activity activity = activityMapper.findById(id);
        if (activity == null) {
            throw new BusinessException("活动不存在");
        }
        return activity;
    }

    @Override
    @Cacheable("activities")
    public List<Activity> findAll() {
        return activityMapper.findAll();
    }

    @Override
    public List<Activity> findByClubId(Long clubId) {
        return activityMapper.findByClubId(clubId);
    }

    @Override
    public List<Activity> findByStatus(String status) {
        return activityMapper.findByStatus(status);
    }

    @Override
    public List<Activity> search(String keyword) {
        return activityMapper.search(keyword);
    }

    @Override
    @CacheEvict(value = "activities", allEntries = true)
    public Activity create(Activity activity) {
        if (activity.getStatus() == null || activity.getStatus().isEmpty()) {
            activity.setStatus("DRAFT");
        }
        activity.setCurrentCount(0);
        activityMapper.insert(activity);
        return activity;
    }

    @Override
    @CacheEvict(value = {"activity", "activities"}, allEntries = true)
    public Activity update(Activity activity) {
        Activity existActivity = activityMapper.findById(activity.getId());
        if (existActivity == null) {
            throw new BusinessException("活动不存在");
        }
        activityMapper.update(activity);
        return activityMapper.findById(activity.getId());
    }

    @Override
    @CacheEvict(value = {"activity", "activities"}, allEntries = true)
    public boolean updateStatus(Long id, String status) {
        return activityMapper.updateStatus(id, status) > 0;
    }

    @Override
    @CacheEvict(value = {"activity", "activities"}, allEntries = true)
    public boolean deleteById(Long id) {
        return activityMapper.deleteById(id) > 0;
    }
}