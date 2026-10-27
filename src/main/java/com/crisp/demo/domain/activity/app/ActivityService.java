package com.crisp.demo.domain.activity.app;

import com.crisp.demo.domain.activity.model.Activity;
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

    /** 提交审核：DRAFT / REJECTED → PENDING */
    Activity submitReview(Long id);

    /** 审核结果回写：PENDING → ONGOING（通过）/ REJECTED（驳回），由审批处理器调用 */
    Activity review(Long id, boolean approved);
}
