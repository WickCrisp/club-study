package com.crisp.demo.domain.club.app;

import com.crisp.demo.domain.club.model.Club;
import com.crisp.demo.domain.club.infra.ClubMapper;
import com.crisp.demo.domain.club.app.ClubService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

import com.crisp.demo.shared.error.BusinessException;
import com.crisp.demo.shared.cache.CacheNames;
import com.crisp.demo.shared.event.ClubDeleted;
import com.crisp.demo.shared.event.ClubLeaderChanged;
import com.crisp.demo.shared.event.DomainEventPublisher;

@Service
@RequiredArgsConstructor
public class ClubServiceImpl implements ClubService {

    private final ClubMapper clubMapper;
    private final DomainEventPublisher eventPublisher;

    /** 社团状态取值 */
    private static final java.util.Set<String> CLUB_STATUSES =
            java.util.Set.of("PENDING_REVIEW", "ACTIVE", "REJECTED", "DISBANDED");

    @Override
    @Cacheable(value = CacheNames.CLUB_DETAIL, key = "#id")
    public Club findById(Long id) {
        Club club = clubMapper.findById(id);
        if (club == null) {
            throw new BusinessException("社团不存在");
        }
        return club;
    }

    @Override
    @Cacheable(CacheNames.CLUB_LIST)
    public List<Club> findAll() {
        return clubMapper.findAll();
    }

    @Override
    public List<Club> search(String keyword) {
        return clubMapper.search(keyword);
    }

    @Override
    @CacheEvict(value = CacheNames.CLUB_LIST, allEntries = true)
    public Club create(Club club) {
        if (club.getStatus() == null || club.getStatus().isEmpty()) {
            club.setStatus("ACTIVE");
        }
        try {
            clubMapper.insert(club);
        } catch (DuplicateKeyException e) {
            throw new BusinessException("社团名称已存在：" + club.getName());
        }
        Club saved = clubMapper.findById(club.getId());   // 回读数据库生成的创建时间等字段
        // 负责人需要同步成 LEADER 成员记录 —— 由 membership 域订阅事件完成，本域不碰 club_member 表
        eventPublisher.publish(new ClubLeaderChanged(saved.getId(), null, saved.getLeaderId()));
        return saved;
    }

    @Override
    @CacheEvict(value = {CacheNames.CLUB_DETAIL, CacheNames.CLUB_LIST}, allEntries = true)
    public Club update(Club club) {
        Club existClub = clubMapper.findById(club.getId());
        if (existClub == null) {
            throw new BusinessException("社团不存在");
        }
        try {
            clubMapper.update(club);
        } catch (DuplicateKeyException e) {
            throw new BusinessException("社团名称已存在：" + club.getName());
        }
        Club updated = clubMapper.findById(club.getId());
        if (!Objects.equals(existClub.getLeaderId(), updated.getLeaderId())) {
            eventPublisher.publish(new ClubLeaderChanged(updated.getId(), existClub.getLeaderId(), updated.getLeaderId()));
        }
        return updated;
    }

    /** 变更社团状态：PENDING_REVIEW → ACTIVE / REJECTED，ACTIVE → DISBANDED */
    @Override
    @CacheEvict(value = {CacheNames.CLUB_DETAIL, CacheNames.CLUB_LIST}, allEntries = true)
    public Club updateStatus(Long id, String status) {
        Club exist = clubMapper.findById(id);
        if (exist == null) {
            throw new BusinessException("社团不存在");
        }
        if (!Objects.equals(exist.getStatus(), status)) {
            if (!CLUB_STATUSES.contains(status)) {
                throw new BusinessException("非法的社团状态：" + status);
            }
            clubMapper.updateStatus(id, status);
        }
        return clubMapper.findById(id);
    }
    /**
     * 删除社团
     *
     * 本域只清自己的 club:* 缓存；数据库会级联删除该社团下的活动、成员、报名，
     * 因此发布 ClubDeleted 事件，由 activity 等域各自清理它们自己的缓存
     * —— 这就是「跨域删除告知」：删除方只广播事实，受影响方自己善后。
     */
    @Override
    @CacheEvict(value = {CacheNames.CLUB_DETAIL, CacheNames.CLUB_LIST}, allEntries = true)
    public boolean deleteById(Long id) {
        boolean deleted = clubMapper.deleteById(id) > 0;
        if (deleted) {
            eventPublisher.publish(new ClubDeleted(id));
        }
        return deleted;
    }
}
