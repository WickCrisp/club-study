package com.crisp.demo.service.impl;

import com.crisp.demo.entity.Club;
import com.crisp.demo.mapper.ClubMapper;
import com.crisp.demo.service.ClubService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClubServiceImpl implements ClubService {

    private final ClubMapper clubMapper;

    @Override
    public Club findById(Long id) {
        Club club = clubMapper.findById(id);
        if (club == null) {
            throw new RuntimeException("社团不存在");
        }
        return club;
    }

    @Override
    public List<Club> findAll() {
        return clubMapper.findAll();
    }

    @Override
    public List<Club> search(String keyword) {
        return clubMapper.search(keyword);
    }

    @Override
    public Club create(Club club) {
        if (club.getStatus() == null || club.getStatus().isEmpty()) {
            club.setStatus("ACTIVE");
        }
        clubMapper.insert(club);
        return club;
    }

    @Override
    public Club update(Club club) {
        Club existClub = clubMapper.findById(club.getId());
        if (existClub == null) {
            throw new RuntimeException("社团不存在");
        }
        clubMapper.update(club);
        return clubMapper.findById(club.getId());
    }

    @Override
    public boolean deleteById(Long id) {
        return clubMapper.deleteById(id) > 0;
    }
}