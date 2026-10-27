package com.crisp.demo.domain.club.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 社团分类（管理员维护，社团挂在分类下）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClubCategory implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private Integer sort;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
