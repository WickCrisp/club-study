package com.crisp.demo.domain.assistant.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 活动文案生成入参：负责人只需填最少的几个要素，其余交给模型组织语言 */
@Data
public class CopyRequest {

    @NotBlank(message = "活动名称不能为空")
    @Size(max = 60, message = "活动名称不能超过 60 字")
    private String title;

    private String time;
    private String place;
    private String audience;

    /** 语气：活泼 / 正式 / 简洁，默认活泼 */
    private String tone;
}
