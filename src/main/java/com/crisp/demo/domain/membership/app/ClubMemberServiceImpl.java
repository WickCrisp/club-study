package com.crisp.demo.domain.membership.app;

import com.crisp.demo.domain.membership.model.ClubMember;
import com.crisp.demo.domain.membership.infra.ClubMemberMapper;
import com.crisp.demo.domain.membership.api.MembershipQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import com.crisp.demo.shared.error.BusinessException;

@Service
@RequiredArgsConstructor
public class ClubMemberServiceImpl implements ClubMemberService, MembershipQueryService {

    private final ClubMemberMapper clubMemberMapper;

    @Override
    public ClubMember findById(Long id) {
        ClubMember member = clubMemberMapper.findById(id);
        if (member == null) throw new BusinessException("成员记录不存在");
        return member;
    }

    @Override
    @Transactional
    public ClubMember apply(Long userId, Long clubId, String applyReason) {
        ClubMember existMember = clubMemberMapper.findByUserAndClub(userId, clubId);
        if (existMember != null) {
            if ("PENDING".equals(existMember.getStatus())) {
                throw new BusinessException("您已申请过该社团，正在审核中");
            }
            if ("APPROVED".equals(existMember.getStatus())) {
                throw new BusinessException("您已是该社团成员");
            }
            // QUIT 或 REJECTED → 更新旧记录重新申请
            existMember.setStatus("PENDING");
            existMember.setApplyReason(applyReason);
            existMember.setJoinedAt(null);
            existMember.setQuitAt(null);
            clubMemberMapper.update(existMember);
            return existMember;
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

    /** 直接退出社团 — 仅 APPROVED 状态可退出 */
    @Override
    @Transactional
    public ClubMember quit(Long memberId) {
        ClubMember member = clubMemberMapper.findById(memberId);
        if (member == null) {
            throw new BusinessException("成员记录不存在");
        }
        if (!"APPROVED".equals(member.getStatus())) {
            throw new BusinessException("当前状态不可退出社团");
        }
        clubMemberMapper.quit(memberId);
        return clubMemberMapper.findById(memberId);
    }

    @Override
    @Transactional
    public ClubMember requestQuit(Long memberId) {
        ClubMember member = clubMemberMapper.findById(memberId);
        if (member == null) {
            throw new BusinessException("成员记录不存在");
        }
        if (!"APPROVED".equals(member.getStatus())) {
            throw new BusinessException("当前状态不可申请退出");
        }
        clubMemberMapper.requestQuit(memberId);
        return clubMemberMapper.findById(memberId);
    }

    @Override
    @Transactional
    public ClubMember approveQuit(Long memberId) {
        ClubMember member = clubMemberMapper.findById(memberId);
        if (member == null) {
            throw new BusinessException("成员记录不存在");
        }
        if (!"QUIT_PENDING".equals(member.getStatus())) {
            throw new BusinessException("该成员未申请退出");
        }
        clubMemberMapper.approveQuit(memberId);
        return clubMemberMapper.findById(memberId);
    }

    @Override
    @Transactional
    public ClubMember rejectQuit(Long memberId) {
        ClubMember member = clubMemberMapper.findById(memberId);
        if (member == null) {
            throw new BusinessException("成员记录不存在");
        }
        if (!"QUIT_PENDING".equals(member.getStatus())) {
            throw new BusinessException("该成员未申请退出");
        }
        clubMemberMapper.rejectQuit(memberId);
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
    public List<ClubMember> findQuitPendingByClubId(Long clubId) {
        return clubMemberMapper.findByClubIdAndStatus(clubId, "QUIT_PENDING");
    }

    @Override
    public ClubMember findByUserAndClub(Long userId, Long clubId) {
        return clubMemberMapper.findByUserAndClub(userId, clubId);
    }

    @Override
    public boolean isApproved(Long clubId, Long userId) {
        ClubMember member = clubMemberMapper.findByUserAndClub(userId, clubId);
        return member != null && "APPROVED".equals(member.getStatus());
    }

    @Override
    public boolean deleteById(Long id) {
        return clubMemberMapper.deleteById(id) > 0;
    }
}