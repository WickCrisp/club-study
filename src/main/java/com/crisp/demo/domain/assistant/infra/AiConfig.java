package com.crisp.demo.domain.assistant.infra;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI 助手的基础设施配置
 *
 * 只在这里定义「助手是谁」——系统提示词与默认行为；
 * 工具不在这里注册，因为查询工具需要绑定当前登录用户，按请求传入（见 AssistantService）。
 */
@Configuration
public class AiConfig {

    @Bean
    ChatClient assistantChatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem("""
                        你是「社团活动管理系统」的校园助手，只回答与社团、活动、报名相关的问题。
                        涉及具体数据（有哪些活动、还剩多少名额、我报了什么）时，必须先调用工具查询，
                        不要凭空编造活动名称、时间、名额或报名记录；工具没有返回，就如实说没有查到。
                        回答使用简体中文，控制在 120 字以内；列举时最多 5 条，用短句，不要长篇大论。
                        与社团无关的问题，礼貌说明你只能回答社团相关的问题。
                        """)
                .build();
    }
}
