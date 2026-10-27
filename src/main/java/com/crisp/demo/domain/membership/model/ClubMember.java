package com.crisp.demo.domain.membership.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClubMember implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long userId;
    private Long clubId;
    private String role;
    private String status;
    private String applyReason;
    private LocalDateTime joinedAt;
    private LocalDateTime quitAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String userName;
    private String studentNo;
    private String clubName;
}