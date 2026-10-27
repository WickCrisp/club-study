package com.crisp.demo.domain.membership.api;

/**
 * 成员域的对外查询契约
 *
 * 其他域判断「某人是否为某社团成员」只能走这里，禁止直接注入 ClubMemberMapper。
 */
public interface MembershipQueryService {

    /** 是否为该社团已通过审核的正式成员 */
    boolean isApproved(Long clubId, Long userId);
}
