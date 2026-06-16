package com.crisp.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivitySummary {
    private Long id;
    private Long activityId;
    private String content;
    private String effect;
    private String suggestions;
    private String images;
    private Long creatorId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String activityTitle;
    private String creatorName;
}