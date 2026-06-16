package com.crisp.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Activity {
    private Long id;
    private Long clubId;
    private String title;
    private String description;
    private String location;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer maxParticipants;
    private Integer currentCount;
    private String status;
    private Long creatorId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String clubName;
    private String creatorName;
}