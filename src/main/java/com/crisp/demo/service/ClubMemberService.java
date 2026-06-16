package com.crisp.demo.service;

import com.crisp.demo.entity.ClubMember;
import java.util.List;

public interface ClubMemberService {
    ClubMember apply(Long userId, Long clubId, String applyReason);
    ClubMember approve(Long memberId);
    ClubMember reject(Long memberId);
    ClubMember quit(Long memberId);
    List<ClubMember> findByClubId(Long clubId);
    List<ClubMember> findByUserId(Long userId);
    List<ClubMember> findPendingByClubId(Long clubId);
    ClubMember findByUserAndClub(Long userId, Long clubId);
    boolean deleteById(Long id);
}