package com.crisp.demo.domain.membership.app;

import com.crisp.demo.domain.membership.model.ClubMember;
import java.util.List;

public interface ClubMemberService {
    ClubMember findById(Long id);
    ClubMember apply(Long userId, Long clubId, String applyReason);
    ClubMember approve(Long memberId);
    ClubMember reject(Long memberId);
    ClubMember quit(Long memberId);
    ClubMember requestQuit(Long memberId);
    ClubMember approveQuit(Long memberId);
    ClubMember rejectQuit(Long memberId);
    List<ClubMember> findByClubId(Long clubId);
    List<ClubMember> findByUserId(Long userId);
    List<ClubMember> findPendingByClubId(Long clubId);
    List<ClubMember> findQuitPendingByClubId(Long clubId);
    ClubMember findByUserAndClub(Long userId, Long clubId);
    boolean deleteById(Long id);
}