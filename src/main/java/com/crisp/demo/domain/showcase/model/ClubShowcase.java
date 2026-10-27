package com.crisp.demo.domain.showcase.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 社团风采条目 —— 一个社团可以有多条图文，社团风采页按 clubId 聚合展示
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClubShowcase implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    @NotNull(message = "所属社团不能为空")
    private Long clubId;

    @NotBlank(message = "风采标题不能为空")
    @Size(max = 200, message = "标题不超过200字")
    private String title;

    private String content;
    private String coverUrl;
    private String images;
    private Integer sort;
    private String status;        // DRAFT / PUBLISHED
    private Long creatorId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** 联表展示 */
    private String clubName;
    private String creatorName;
}
