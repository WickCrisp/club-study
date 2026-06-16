package com.crisp.demo.service;

import com.crisp.demo.entity.Activity;
import java.util.List;

public interface ActivityService {
    Activity findById(Long id);
    List<Activity> findAll();
    List<Activity> findByClubId(Long clubId);
    List<Activity> findByStatus(String status);
    List<Activity> search(String keyword);
    Activity create(Activity activity);
    Activity update(Activity activity);
    boolean updateStatus(Long id, String status);
    boolean deleteById(Long id);
}