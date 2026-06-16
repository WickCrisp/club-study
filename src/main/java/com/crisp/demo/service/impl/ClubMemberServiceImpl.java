package com.crisp.demo.service.impl;

import com.crisp.demo.entity.ClubMember;
import com.crisp.demo.mapper.ClubMemberMapper;
import com.crisp.demo.service.ClubMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClubMemberServiceImpl implements ClubMemberService {

    private final ClubMemberMapper clubMemberMapper;

    @Override
    @Transactional
    public ClubMember apply(Long userId, Long clubId, String applyReason) {
        ClubMember existMember = clubMemberMapper.findByUserAndClub(userId, clubId);
        if (existMember != null) {
            throw new RuntimeException("您已申请过该社团，请勿重复申请");
        }
        ClubMember member = new ClubMember();
        member.setUserId(userId);
        member.setClubId(clubId);
        member.setRole("MEMBER");
        member.setStatus("PENDING");
        member.setApplyReason(applyReason);
        clubMemberMapper.insert(member);
        return member;
    }

    @Override
    @Transactional
    public ClubMember approve(Long memberId) {
        clubMemberMapper.approve(memberId);
        return clubMemberMapper.findById(memberId);
    }

    @Override
    @Transactional
    public ClubMember reject(Long memberId) {
        clubMemberMapper.reject(memberId);
        return clubMemberMapper.findById(memberId);
    }

    @Override
    @Transactional
    public ClubMember quit(Long memberId) {
        clubMemberMapper.quit(memberId);
        return clubMemberMapper.findById(memberId);
    }

    @Override
    public List<ClubMember> findByClubId(Long clubId) {
        return clubMemberMapper.findByClubId(clubId);
    }

    @Override
    public List<ClubMember> findByUserId(Long userId) {
        return clubMemberMapper.findByUserId(userId);
    }

    @Override
    public List<ClubMember> findPendingByClubId(Long clubId) {
        return clubMemberMapper.findByClubIdAndStatus(clubId, "PENDING");
    }

    @Override
    public ClubMember findByUserAndClub(Long userId, Long clubId) {
        return clubMemberMapper.findByUserAndClub(userId, clubId);
    }

    @Override
    public boolean deleteById(Long id) {
        return clubMemberMapper.deleteById(id) > 0;
    }
}