package com.crisp.demo.domain.assistant.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 一轮历史对话（只保留 user / assistant 两种角色，由前端回传最近 5 轮） */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatTurn {
    private String role;
    private String content;
}
