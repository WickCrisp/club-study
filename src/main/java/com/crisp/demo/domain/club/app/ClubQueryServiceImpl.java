package com.crisp.demo.domain.club.app;

import com.crisp.demo.domain.club.api.ClubQueryService;
import com.crisp.demo.domain.club.infra.ClubMapper;
import com.crisp.demo.domain.club.model.Club;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClubQueryServiceImpl implements ClubQueryService {

    private final ClubMapper clubMapper;

    @Override
    public Club findById(Long id) {
        return clubMapper.findById(id);
    }

    @Override
    public List<Club> findAll() {
        return clubMapper.findAll();
    }

    @Override
    public List<Club> findByLeaderId(Long leaderId) {
        return clubMapper.findByLeaderId(leaderId);
    }

    @Override
    public Long leaderIdOf(Long clubId) {
        Club club = clubMapper.findById(clubId);
        return club == null ? null : club.getLeaderId();
    }
}
