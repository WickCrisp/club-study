package com.crisp.demo.domain.activity.app;

import com.crisp.demo.domain.activity.api.ActivityOccupancyPort;
import com.crisp.demo.domain.activity.api.ActivityQueryService;
import com.crisp.demo.domain.activity.infra.ActivityMapper;
import com.crisp.demo.domain.activity.model.Activity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityQueryServiceImpl implements ActivityQueryService, ActivityOccupancyPort {

    private final ActivityMapper activityMapper;

    // ===== ActivityQueryService =====

    @Override
    public Activity findById(Long id) {
        return activityMapper.findById(id);
    }

    @Override
    public List<Activity> findAll() {
        return activityMapper.findAll();
    }

    @Override
    public List<Activity> findByCreatorId(Long creatorId) {
        return activityMapper.findByCreatorId(creatorId);
    }

    @Override
    public List<Activity> findByClubId(Long clubId) {
        return activityMapper.findByClubId(clubId);
    }

    @Override
    public String statusOf(Long activityId) {
        Activity activity = activityMapper.findById(activityId);
        return activity == null ? null : activity.getStatus();
    }

    @Override
    public Long clubIdOf(Long activityId) {
        Activity activity = activityMapper.findById(activityId);
        return activity == null ? null : activity.getClubId();
    }

    // ===== ActivityOccupancyPort =====

    @Override
    public boolean occupy(Long activityId) {
        return activityMapper.incrementCount(activityId) > 0;
    }

    @Override
    public boolean release(Long activityId) {
        return activityMapper.decrementCount(activityId) > 0;
    }
}
