package com.crisp.demo.domain.registration.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityRegistration implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long activityId;
    private Long userId;
    private String status;
    private LocalDateTime registeredAt;
    private LocalDateTime cancelledAt;

    private String userName;
    private String studentNo;
    private String activityTitle;
    private Long clubId;
    private String clubName;
    private Long leaderId;
}