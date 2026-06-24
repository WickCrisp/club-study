package com.crisp.demo.service.impl;

import com.crisp.demo.entity.Club;
import com.crisp.demo.mapper.ClubMapper;
import com.crisp.demo.service.ClubService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

import com.crisp.demo.common.BusinessException;

@Service
@RequiredArgsConstructor
public class ClubServiceImpl implements ClubService {

    private final ClubMapper clubMapper;

    @Override
    @Cacheable(value = "club", key = "#id")
    public Club findById(Long id) {
        Club club = clubMapper.findById(id);
        if (club == null) {
            throw new BusinessException("社团不存在");
        }
        return club;
    }

    @Override
    @Cacheable("clubs")
    public List<Club> findAll() {
        return clubMapper.findAll();
    }

    @Override
    public List<Club> search(String keyword) {
        return clubMapper.search(keyword);
    }

    @Override
    @CacheEvict(value = "clubs", allEntries = true)
    public Club create(Club club) {
        if (club.getStatus() == null || club.getStatus().isEmpty()) {
            club.setStatus("ACTIVE");
        }
        clubMapper.insert(club);
        return club;
    }

    @Override
    @CacheEvict(value = {"club", "clubs"}, allEntries = true)
    public Club update(Club club) {
        Club existClub = clubMapper.findById(club.getId());
        if (existClub == null) {
            throw new BusinessException("社团不存在");
        }
        clubMapper.update(club);
        return clubMapper.findById(club.getId());
    }

    @Override
    @CacheEvict(value = {"club", "clubs"}, allEntries = true)
    public boolean deleteById(Long id) {
        return clubMapper.deleteById(id) > 0;
    }
}