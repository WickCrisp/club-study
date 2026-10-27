package com.crisp.demo.domain.showcase.app;

import com.crisp.demo.domain.club.api.ClubQueryService;
import com.crisp.demo.domain.club.model.Club;
import com.crisp.demo.domain.showcase.infra.ClubShowcaseMapper;
import com.crisp.demo.domain.showcase.model.ClubShowcase;
import com.crisp.demo.shared.error.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClubShowcaseService {

    private final ClubShowcaseMapper showcaseMapper;
    private final ClubQueryService clubQueryService;

    public List<ClubShowcase> findByClubId(Long clubId) {
        return showcaseMapper.findByClubId(clubId);
    }

    public List<ClubShowcase> findAllByClubId(Long clubId) {
        return showcaseMapper.findAllByClubId(clubId);
    }

    public List<ClubShowcase> findAllPublished() {
        return showcaseMapper.findAllPublished();
    }

    public ClubShowcase findById(Long id) {
        ClubShowcase showcase = showcaseMapper.findById(id);
        if (showcase == null) {
            throw new BusinessException("风采内容不存在");
        }
        return showcase;
    }

    @Transactional
    public ClubShowcase create(ClubShowcase showcase, Long creatorId) {
        Club club = clubQueryService.findById(showcase.getClubId());
        if (club == null) {
            throw new BusinessException("社团不存在");
        }
        showcase.setCreatorId(creatorId);
        if (showcase.getSort() == null) showcase.setSort(0);
        if (showcase.getStatus() == null || showcase.getStatus().isEmpty()) showcase.setStatus("PUBLISHED");
        showcaseMapper.insert(showcase);
        return showcaseMapper.findById(showcase.getId());
    }

    @Transactional
    public ClubShowcase update(ClubShowcase showcase) {
        ClubShowcase exist = findById(showcase.getId());
        if (showcase.getTitle() == null || showcase.getTitle().trim().isEmpty()) {
            showcase.setTitle(exist.getTitle());
        }
        if (showcase.getSort() == null) showcase.setSort(exist.getSort());
        if (showcase.getStatus() == null || showcase.getStatus().isEmpty()) {
            showcase.setStatus(exist.getStatus());
        }
        showcaseMapper.update(showcase);
        return showcaseMapper.findById(showcase.getId());
    }

    @Transactional
    public boolean deleteById(Long id) {
        return showcaseMapper.deleteById(id) > 0;
    }

    public int countPublishedByClub(Long clubId) {
        return showcaseMapper.countPublishedByClub(clubId);
    }

    public int countPublished() {
        return showcaseMapper.countPublished();
    }
}
