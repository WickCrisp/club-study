package com.crisp.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Club {
    private Long id;
    private String name;
    private String description;
    private String logoUrl;
    private Long leaderId;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String leaderName;
}