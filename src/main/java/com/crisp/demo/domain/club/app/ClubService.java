package com.crisp.demo.domain.club.app;

import com.crisp.demo.domain.club.model.Club;
import java.util.List;

public interface ClubService {
    Club findById(Long id);
    List<Club> findAll();
    List<Club> search(String keyword);
    Club create(Club club);
    Club update(Club club);
    boolean deleteById(Long id);
    /** 变更社团状态（审核通过/驳回/解散），由审批处理器或管理员调用 */
    Club updateStatus(Long id, String status);
}