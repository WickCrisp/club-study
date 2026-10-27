package com.crisp.demo.domain.membership.infra;

import com.crisp.demo.domain.membership.model.ClubMember;
import com.crisp.demo.shared.event.ClubLeaderChanged;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 社团负责人变更时的成员记录同步（membership 域自己负责，club 域无需知道 club_member 表）
 *
 * 新负责人 → LEADER / APPROVED（没有记录则补一条）
 * 旧负责人 → MEMBER
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ClubLeaderSyncListener {

    private final ClubMemberMapper clubMemberMapper;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onLeaderChanged(ClubLeaderChanged event) {
        if (Objects.equals(event.oldLeaderId(), event.newLeaderId())) {
            return;
        }
        demoteOldLeader(event);
        promoteNewLeader(event);
    }

    private void demoteOldLeader(ClubLeaderChanged event) {
        if (event.oldLeaderId() == null) {
            return;
        }
        ClubMember old = clubMemberMapper.findByUserAndClub(event.oldLeaderId(), event.clubId());
        if (old != null && "LEADER".equals(old.getRole())) {
            old.setRole("MEMBER");
            clubMemberMapper.update(old);
            log.debug("社团 {} 旧负责人 {} 降级为 MEMBER", event.clubId(), event.oldLeaderId());
        }
    }

    private void promoteNewLeader(ClubLeaderChanged event) {
        if (event.newLeaderId() == null) {
            return;
        }
        ClubMember exist = clubMemberMapper.findByUserAndClub(event.newLeaderId(), event.clubId());
        if (exist == null) {
            ClubMember leader = new ClubMember();
            leader.setUserId(event.newLeaderId());
            leader.setClubId(event.clubId());
            leader.setRole("LEADER");
            leader.setStatus("APPROVED");
            leader.setJoinedAt(LocalDateTime.now());
            clubMemberMapper.insert(leader);
        } else {
            exist.setRole("LEADER");
            exist.setStatus("APPROVED");
            if (exist.getJoinedAt() == null) {
                exist.setJoinedAt(LocalDateTime.now());
            }
            clubMemberMapper.update(exist);
        }
        log.debug("社团 {} 新负责人 {} 升级为 LEADER", event.clubId(), event.newLeaderId());
    }
}
