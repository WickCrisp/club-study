package com.crisp.demo.domain.assistant.api;

import com.crisp.demo.domain.assistant.api.dto.ActivityCopy;
import com.crisp.demo.domain.assistant.api.dto.ChatRequest;
import com.crisp.demo.domain.assistant.api.dto.CopyRequest;
import com.crisp.demo.domain.assistant.app.AssistantService;
import com.crisp.demo.domain.identity.model.User;
import com.crisp.demo.shared.result.Result;
import com.crisp.demo.shared.security.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * 前台 AI 助手
 *
 * 两个接口都要求登录：登录态由 JwtAuthInterceptor 校验，用户身份由 @CurrentUser 注入，
 * 前端传来的任何 userId 都不作数。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/assistant")
public class AssistantController {

    private final AssistantService assistantService;

    /** 流式问答（SSE）：前端用 fetch 逐块读取，不能用 axios（会缓冲整段响应） */
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(@Valid @RequestBody ChatRequest request, @CurrentUser User user) {
        return assistantService.chat(request, user);
    }

    /** 活动文案生成：返回简介 + 亮点 + 注意事项三段结构化结果 */
    @PostMapping("/copy")
    public Result<ActivityCopy> copy(@Valid @RequestBody CopyRequest request, @CurrentUser User user) {
        return Result.success("生成成功", assistantService.generateCopy(request, user));
    }
}
