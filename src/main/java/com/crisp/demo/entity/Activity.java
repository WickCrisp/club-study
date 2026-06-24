package com.crisp.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Activity implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;

    @NotNull(message = "所属社团不能为空")                    // 活动必须归属一个社团
    private Long clubId;

    @NotBlank(message = "活动标题不能为空")                    // 活动名称
    @Size(max = 200, message = "标题不超过200字")
    private String title;

    @Size(max = 2000, message = "活动简介不超过2000字")         // 活动描述
    private String description;

    @NotBlank(message = "活动地点不能为空")                    // 举办地点
    @Size(max = 200, message = "地点不超过200字")
    private String location;

    @NotNull(message = "开始时间不能为空")                     // 活动开始时间
    private LocalDateTime startTime;

    @NotNull(message = "结束时间不能为空")                     // 活动结束时间
    private LocalDateTime endTime;

    @Min(value = 0, message = "人数限制不能为负数")              // 0 表示不限
    private Integer maxParticipants;

    private Integer currentCount;                              // 当前报名人数（系统维护）
    private String status;                                     // DRAFT / ONGOING / FINISHED / CANCELLED
    private Long creatorId;                                    // 发布人ID
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String clubName;                                   // 联表查询：社团名
    private String creatorName;                                // 联表查询：发布人
}