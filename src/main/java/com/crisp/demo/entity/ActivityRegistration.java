package com.crisp.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityRegistration {
    private Long id;
    private Long activityId;
    private Long userId;
    private String status;
    private LocalDateTime registeredAt;
    private LocalDateTime cancelledAt;

    private String userName;
    private String studentNo;
    private String activityTitle;
}