package com.crisp.demo.domain.announcement.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 全校公告（仅管理员发布，所有用户可见）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Announcement implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    @NotBlank(message = "公告标题不能为空")
    @Size(max = 200, message = "标题不超过200字")
    private String title;

    @NotBlank(message = "公告内容不能为空")
    private String content;

    private Boolean pinned;
    private String status;          // DRAFT / PUBLISHED
    private Long publisherId;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** 联表展示：发布人姓名 */
    private String publisherName;
}
