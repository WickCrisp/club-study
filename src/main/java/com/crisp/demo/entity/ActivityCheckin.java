package com.crisp.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityCheckin {
    private Long id;
    private Long registrationId;
    private Long activityId;
    private Long userId;
    private LocalDateTime checkinTime;
    private String checkinStatus;
    private LocalDateTime checkoutTime;
    private String checkoutStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String userName;
    private String studentNo;
    private String activityTitle;
}