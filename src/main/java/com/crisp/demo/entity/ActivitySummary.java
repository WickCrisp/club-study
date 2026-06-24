package com.crisp.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivitySummary implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;

    @NotNull(message = "活动ID不能为空")                    // 关联的活动
    private Long activityId;

    @NotBlank(message = "活动总结内容不能为空")               // 总结正文
    @Size(max = 5000, message = "总结内容不超过5000字")
    private String content;

    @Size(max = 2000, message = "效果评估不超过2000字")        // 效果评估
    private String effect;

    @Size(max = 2000, message = "改进建议不超过2000字")        // 改进建议
    private String suggestions;

    @Size(max = 2000, message = "图片URL不超过2000字")         // 图片URL列表
    private String images;

    private Long creatorId;                                 // 创建人ID
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String activityTitle;                           // 联表查询：活动标题
    private String creatorName;                             // 联表查询：创建人
}