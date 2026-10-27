package com.crisp.demo.domain.announcement.app;

import com.crisp.demo.domain.announcement.infra.AnnouncementMapper;
import com.crisp.demo.domain.announcement.model.Announcement;
import com.crisp.demo.shared.error.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnnouncementService {

    private final AnnouncementMapper announcementMapper;

    public List<Announcement> findPublished() {
        return announcementMapper.findPublished();
    }

    public List<Announcement> findAll() {
        return announcementMapper.findAll();
    }

    public Announcement findById(Long id) {
        Announcement announcement = announcementMapper.findById(id);
        if (announcement == null) {
            throw new BusinessException("公告不存在");
        }
        return announcement;
    }

    @Transactional
    public Announcement create(Announcement announcement, Long publisherId) {
        announcement.setPublisherId(publisherId);
        if (announcement.getPinned() == null) {
            announcement.setPinned(false);
        }
        if (announcement.getStatus() == null || announcement.getStatus().isEmpty()) {
            announcement.setStatus("DRAFT");
        }
        if ("PUBLISHED".equals(announcement.getStatus())) {
            announcement.setPublishedAt(LocalDateTime.now());
        }
        announcementMapper.insert(announcement);
        return announcementMapper.findById(announcement.getId());
    }

    @Transactional
    public Announcement update(Announcement announcement) {
        Announcement exist = findById(announcement.getId());
        if (announcement.getTitle() == null || announcement.getTitle().trim().isEmpty()) {
            announcement.setTitle(exist.getTitle());
        }
        if (announcement.getContent() == null || announcement.getContent().isEmpty()) {
            announcement.setContent(exist.getContent());
        }
        if (announcement.getPinned() == null) {
            announcement.setPinned(exist.getPinned());
        }
        if (announcement.getStatus() == null || announcement.getStatus().isEmpty()) {
            announcement.setStatus(exist.getStatus());
        }
        // 首次发布时记录发布时间
        announcement.setPublishedAt("PUBLISHED".equals(announcement.getStatus())
                ? (exist.getPublishedAt() != null ? exist.getPublishedAt() : LocalDateTime.now())
                : exist.getPublishedAt());
        announcementMapper.update(announcement);
        return announcementMapper.findById(announcement.getId());
    }

    @Transactional
    public Announcement changeStatus(Long id, String status) {
        if (!"DRAFT".equals(status) && !"PUBLISHED".equals(status)) {
            throw new BusinessException("非法的公告状态：" + status);
        }
        Announcement exist = findById(id);
        Announcement update = new Announcement();
        update.setId(id);
        update.setTitle(exist.getTitle());
        update.setContent(exist.getContent());
        update.setPinned(exist.getPinned());
        update.setStatus(status);
        update.setPublishedAt("PUBLISHED".equals(status)
                ? (exist.getPublishedAt() != null ? exist.getPublishedAt() : LocalDateTime.now())
                : exist.getPublishedAt());
        announcementMapper.update(update);
        return announcementMapper.findById(id);
    }

    @Transactional
    public boolean deleteById(Long id) {
        return announcementMapper.deleteById(id) > 0;
    }

    public int countPublished() {
        return announcementMapper.countPublished();
    }
}
