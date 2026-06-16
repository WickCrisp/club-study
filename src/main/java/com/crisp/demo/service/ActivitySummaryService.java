package com.crisp.demo.service;

import com.crisp.demo.entity.ActivitySummary;
import java.util.List;

public interface ActivitySummaryService {
    ActivitySummary findById(Long id);
    ActivitySummary findByActivityId(Long activityId);
    List<ActivitySummary> findAll();
    ActivitySummary create(ActivitySummary summary);
    ActivitySummary update(ActivitySummary summary);
    boolean deleteById(Long id);
}