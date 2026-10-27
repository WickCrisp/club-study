package com.crisp.demo.domain.assistant.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/** 对话请求：message 为本次提问，history 为前端保留的最近若干轮上下文 */
@Data
public class ChatRequest {

    @NotBlank(message = "问题不能为空")
    @Size(max = 500, message = "问题不能超过 500 字")
    private String message;

    private List<ChatTurn> history;
}
