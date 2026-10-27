package com.crisp.demo.domain.assistant.app;

import com.crisp.demo.domain.assistant.api.dto.ActivityCopy;
import com.crisp.demo.domain.assistant.api.dto.ChatRequest;
import com.crisp.demo.domain.assistant.api.dto.ChatTurn;
import com.crisp.demo.domain.assistant.api.dto.CopyRequest;
import com.crisp.demo.domain.assistant.app.tools.ClubAssistantTools;
import com.crisp.demo.domain.activity.api.ActivityQueryService;
import com.crisp.demo.domain.club.api.ClubQueryService;
import com.crisp.demo.domain.identity.model.User;
import com.crisp.demo.domain.registration.api.RegistrationQueryService;
import com.crisp.demo.shared.error.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI 助手应用服务
 *
 * 三件事：把用户问题组装成 Prompt（带最近 5 轮上下文）、按当前用户绑定只读工具、兜住模型侧异常。
 * 注意：用户身份只从登录态取，绝不接受模型或前端传入的 userId。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AssistantService {

    /** 前端最多回传 5 轮（10 条消息），超出直接截断，避免 token 失控 */
    private static final int MAX_HISTORY_MESSAGES = 10;
    /** 单用户每分钟最多 20 次，防止把 API 额度刷爆 */
    private static final int RATE_LIMIT_PER_MINUTE = 20;
    private static final Duration STREAM_TIMEOUT = Duration.ofSeconds(60);

    private final ChatClient assistantChatClient;
    private final ClubQueryService clubQueryService;
    private final ActivityQueryService activityQueryService;
    private final RegistrationQueryService registrationQueryService;

    private final Map<Long, Deque<Long>> rateBuckets = new ConcurrentHashMap<>();

    /** 流式对话：返回的每个字符串都是一小段增量文本 */
    public Flux<String> chat(ChatRequest request, User user) {
        if (user == null) {
            throw new BusinessException("请先登录后再使用助手");
        }
        checkRate(user.getId());

        String question = request.getMessage() == null ? "" : request.getMessage().trim();
        if (question.isEmpty()) {
            throw new BusinessException("问题不能为空");
        }

        ClubAssistantTools tools = new ClubAssistantTools(
                clubQueryService, activityQueryService, registrationQueryService, user.getId());

        Message[] history = toMessages(request.getHistory());

        return assistantChatClient.prompt()
                .messages(history)
                .user(question)
                .tools(tools)
                .stream()
                .content()
                .timeout(STREAM_TIMEOUT)
                .onErrorResume(e -> {
                    log.warn("AI 助手调用失败：{}", e.toString());
                    return Flux.just(friendlyMessage(e));
                });
    }

    /** 结构化文案生成：不流式，直接回三段结构化结果，前端渲染成卡片 */
    public ActivityCopy generateCopy(CopyRequest request, User user) {
        if (user == null) {
            throw new BusinessException("请先登录后再使用助手");
        }
        checkRate(user.getId());

        String prompt = """
                你是高校社团的活动宣传助手。请根据下面的信息，写一段招新/宣传文案。
                活动名称：%s
                活动时间：%s
                活动地点：%s
                面向对象：%s
                语气：%s

                要求：
                1) intro：80~120 字，有画面感，说明活动做什么、为什么值得来；
                2) highlights：3 条，每条不超过 20 字；
                3) notes：2~3 条报名须知，例如需要自带什么、名额是否有限；
                4) 不要编造信息里没有的时间、地点、奖项或嘉宾；
                5) 只输出 JSON，不要 Markdown 代码块，不要解释。
                """.formatted(
                request.getTitle(),
                blankTo(request.getTime(), "待定"),
                blankTo(request.getPlace(), "待定"),
                blankTo(request.getAudience(), "全体在校学生"),
                blankTo(request.getTone(), "活泼"));

        try {
            ActivityCopy copy = assistantChatClient.prompt().user(prompt).call().entity(ActivityCopy.class);
            if (copy == null || copy.getIntro() == null || copy.getIntro().isBlank()) {
                throw new BusinessException("文案生成失败，请换个说法再试一次");
            }
            return copy;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.warn("文案生成失败：{}", e.toString());
            throw new BusinessException("文案生成失败，请稍后再试");
        }
    }

    private Message[] toMessages(List<ChatTurn> history) {
        if (history == null || history.isEmpty()) {
            return new Message[0];
        }
        List<ChatTurn> turns = history.subList(Math.max(0, history.size() - MAX_HISTORY_MESSAGES), history.size());
        List<Message> messages = new ArrayList<>(turns.size());
        for (ChatTurn turn : turns) {
            if (turn == null || turn.getContent() == null || turn.getContent().isBlank()) {
                continue;
            }
            if ("assistant".equalsIgnoreCase(turn.getRole())) {
                messages.add(new AssistantMessage(turn.getContent()));
            } else {
                messages.add(new UserMessage(turn.getContent()));
            }
        }
        return messages.toArray(new Message[0]);
    }

    private void checkRate(Long userId) {
        long now = System.currentTimeMillis();
        Deque<Long> bucket = rateBuckets.computeIfAbsent(userId, k -> new ArrayDeque<>());
        synchronized (bucket) {
            while (!bucket.isEmpty() && now - bucket.peekFirst() > 60_000L) {
                bucket.pollFirst();
            }
            if (bucket.size() >= RATE_LIMIT_PER_MINUTE) {
                throw new BusinessException("发送太频繁了，休息一分钟再试");
            }
            bucket.addLast(now);
        }
    }

    /** 把模型侧的异常翻译成用户能看懂的一句话，不暴露密钥、堆栈与上游细节 */
    private String friendlyMessage(Throwable e) {
        String text = String.valueOf(e.getMessage());
        if (text.contains("401") || text.toLowerCase().contains("api key")
                || text.toLowerCase().contains("unauthorized")) {
            return "AI 服务还没配置密钥，请管理员设置 DEEPSEEK_API_KEY 后再试。";
        }
        if (e instanceof java.util.concurrent.TimeoutException || text.contains("timeout")) {
            return "这次回答超时了，请稍后再问一次。";
        }
        return "助手暂时不可用，请稍后再试。";
    }

    private String blankTo(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}
