package com.crisp.demo.domain.club.api;

import com.crisp.demo.domain.club.model.Club;

import java.util.List;

/**
 * 社团域的对外查询契约
 *
 * 其他域**只能**通过本接口读取社团数据，禁止直接注入 ClubMapper。
 */
public interface ClubQueryService {

    /** 按 id 查询，不存在返回 null（调用方自行决定是否报错） */
    Club findById(Long id);

    List<Club> findAll();

    List<Club> findByLeaderId(Long leaderId);

    /** 社团负责人 id；社团不存在或没有负责人时返回 null */
    Long leaderIdOf(Long clubId);
}
