package com.crisp.demo.service;

import com.crisp.demo.entity.Club;
import java.util.List;

public interface ClubService {
    Club findById(Long id);
    List<Club> findAll();
    List<Club> search(String keyword);
    Club create(Club club);
    Club update(Club club);
    boolean deleteById(Long id);
}