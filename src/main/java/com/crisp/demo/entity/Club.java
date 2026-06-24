package com.crisp.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Club implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;

    @NotBlank(message = "社团名称不能为空")                    // 社团唯一名称
    @Size(max = 100, message = "社团名称不超过100字")
    private String name;

    @Size(max = 500, message = "社团简介不超过500字")           // 社团描述
    private String description;

    private Long leaderId;                                     // 负责人用户ID
    private String status;                                     // ACTIVE / DISBANDED
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String leaderName;                                 // 联表查询：负责人姓名
}