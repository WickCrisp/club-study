package com.crisp.demo.domain.assistant.api.dto;

import lombok.Data;

import java.util.List;

/** 活动文案生成结果：模型按这三个字段返回结构化 JSON，前端直接渲染卡片 */
@Data
public class ActivityCopy {

    /** 一句话简介，80~120 字 */
    private String intro;

    /** 活动亮点，3 条 */
    private List<String> highlights;

    /** 报名须知 / 注意事项 */
    private List<String> notes;
}
