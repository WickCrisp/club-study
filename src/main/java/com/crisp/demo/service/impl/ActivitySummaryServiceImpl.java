package com.crisp.demo.service.impl;

import com.crisp.demo.entity.ActivitySummary;
import com.crisp.demo.mapper.ActivitySummaryMapper;
import com.crisp.demo.service.ActivitySummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import com.crisp.demo.common.BusinessException;

@Service
@RequiredArgsConstructor
public class ActivitySummaryServiceImpl implements ActivitySummaryService {

    private final ActivitySummaryMapper summaryMapper;

    @Override
    public ActivitySummary findById(Long id) {
        ActivitySummary summary = summaryMapper.findById(id);
        if (summary == null) {
            throw new BusinessException("活动总结不存在");
        }
        return summary;
    }

    @Override
    public ActivitySummary findByActivityId(Long activityId) {
        return summaryMapper.findByActivityId(activityId);
    }

    @Override
    public List<ActivitySummary> findAll() {
        return summaryMapper.findAll();
    }

    @Override
    public ActivitySummary create(ActivitySummary summary) {
        ActivitySummary existSummary = summaryMapper.findByActivityId(summary.getActivityId());
        if (existSummary != null) {
            throw new BusinessException("该活动已有总结，请勿重复创建");
        }
        summaryMapper.insert(summary);
        return summary;
    }

    @Override
    public ActivitySummary update(ActivitySummary summary) {
        ActivitySummary existSummary = summaryMapper.findById(summary.getId());
        if (existSummary == null) {
            throw new BusinessException("活动总结不存在");
        }
        summaryMapper.update(summary);
        return summaryMapper.findById(summary.getId());
    }

    @Override
    public boolean deleteById(Long id) {
        return summaryMapper.deleteById(id) > 0;
    }
}