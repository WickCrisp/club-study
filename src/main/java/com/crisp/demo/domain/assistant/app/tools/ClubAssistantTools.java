package com.crisp.demo.domain.assistant.app.tools;

import com.crisp.demo.domain.activity.api.ActivityQueryService;
import com.crisp.demo.domain.activity.model.Activity;
import com.crisp.demo.domain.club.api.ClubQueryService;
import com.crisp.demo.domain.club.model.Club;
import com.crisp.demo.domain.registration.api.RegistrationQueryService;
import com.crisp.demo.domain.registration.model.ActivityRegistration;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

/**
 * 提供给模型的只读工具
 *
 * 设计要点：
 * 1) 只有查询，没有任何写操作——模型不能替用户报名、审批或改数据；
 * 2) userId 由 {@link com.crisp.demo.domain.assistant.app.AssistantService} 构造时注入，
 *    绝不由模型传入，避免「让模型查别人的报名」这类越权；
 * 3) 返回值做裁剪（最多 5 条、只留关键字段），控制 token 与幻觉空间。
 */
public class ClubAssistantTools {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("MM-dd HH:mm");
    private static final int MAX_ITEMS = 5;

    private final ClubQueryService clubQueryService;
    private final ActivityQueryService activityQueryService;
    private final RegistrationQueryService registrationQueryService;
    private final Long userId;

    public ClubAssistantTools(ClubQueryService clubQueryService,
                              ActivityQueryService activityQueryService,
                              RegistrationQueryService registrationQueryService,
                              Long userId) {
        this.clubQueryService = clubQueryService;
        this.activityQueryService = activityQueryService;
        this.registrationQueryService = registrationQueryService;
        this.userId = userId;
    }

    @Tool(description = "查询当前正在报名、可以直接参加的活动，返回活动名称、时间、地点与剩余名额。用户问“有什么活动”“最近能报什么”时调用")
    public List<String> listOpenActivities() {
        return activityQueryService.findAll().stream()
                .filter(a -> "ONGOING".equals(a.getStatus()))
                .limit(MAX_ITEMS)
                .map(a -> "%s（%s，%s，%s）".formatted(
                        a.getTitle(),
                        a.getStartTime() == null ? "时间待定" : a.getStartTime().format(TIME_FMT),
                        a.getLocation() == null ? "地点待定" : a.getLocation(),
                        remainingText(a)))
                .toList();
    }

    @Tool(description = "按关键词搜索社团，返回社团名称、简介与负责人。用户问某个社团是做什么的时候调用")
    public List<String> searchClubs(
            @ToolParam(description = "搜索关键词，可以是社团名称的一部分，例如“算法”“篮球”") String keyword) {
        String kw = keyword == null ? "" : keyword.trim();
        return clubQueryService.findAll().stream()
                .filter(c -> kw.isEmpty()
                        || (c.getName() != null && c.getName().contains(kw))
                        || (c.getDescription() != null && c.getDescription().contains(kw)))
                .limit(MAX_ITEMS)
                .map(c -> "%s（%s，负责人：%s）".formatted(
                        c.getName(),
                        c.getDescription() == null ? "暂无简介" : c.getDescription(),
                        c.getLeaderName() == null ? "待定" : c.getLeaderName()))
                .toList();
    }

    @Tool(description = "查询当前登录用户自己的报名记录，返回活动名称与报名状态。用户问“我报了什么”“我的报名”时调用；不需要传用户 id")
    public List<String> myRegistrations() {
        List<ActivityRegistration> mine = registrationQueryService.findByUserId(userId);
        if (mine == null || mine.isEmpty()) {
            return List.of("当前没有报名记录");
        }
        return mine.stream()
                .limit(MAX_ITEMS)
                .map(r -> {
                    Activity activity = activityQueryService.findById(r.getActivityId());
                    return "%s（%s）".formatted(
                            activity == null ? "已下架活动" : activity.getTitle(),
                            switch (Objects.requireNonNullElse(r.getStatus(), "")) {
                                case "REGISTERED" -> "已报名";
                                case "CANCEL_PENDING" -> "取消待审批";
                                case "CANCELLED" -> "已取消";
                                default -> r.getStatus();
                            });
                })
                .toList();
    }

    @Tool(description = "查询指定活动还剩多少名额，参数是活动 id。用户问“还剩几个名额”“还能报吗”且能确定活动时调用")
    public String activitySeats(
            @ToolParam(description = "活动 id，必须是数字") Long activityId) {
        Activity activity = activityQueryService.findById(activityId);
        if (activity == null) {
            return "没有找到该活动";
        }
        int max = activity.getMaxParticipants() == null ? 0 : activity.getMaxParticipants();
        int current = activity.getCurrentCount() == null ? 0 : activity.getCurrentCount();
        if (max == 0) {
            return "%s：不限名额，当前已报 %d 人".formatted(activity.getTitle(), current);
        }
        return "%s：名额上限 %d，已报 %d，剩余 %d".formatted(activity.getTitle(), max, current, remaining(activity));
    }

    /** 0 表示不限名额，用文字表达，避免模型把 -1 当成真实剩余数 */
    private String remainingText(Activity activity) {
        int left = remaining(activity);
        return left < 0 ? "不限名额" : "剩余 " + left + " 个名额";
    }

    private int remaining(Activity activity) {
        int max = activity.getMaxParticipants() == null ? 0 : activity.getMaxParticipants();
        int current = activity.getCurrentCount() == null ? 0 : activity.getCurrentCount();
        // 0 表示不限名额
        return max == 0 ? -1 : Math.max(max - current, 0);
    }
}
