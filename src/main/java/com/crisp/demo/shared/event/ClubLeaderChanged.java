package com.crisp.demo.shared.event;

/**
 * 社团负责人变更（创建社团时为 null → 首任负责人）
 *
 * 发布方：club 域
 * 订阅方：membership 域（把新负责人升级为 LEADER/APPROVED 成员、把旧负责人降级为 MEMBER）
 *
 * 这样 club 域不需要反向依赖 membership 域，跨域联动通过事件完成。
 */
public record ClubLeaderChanged(Long clubId, Long oldLeaderId, Long newLeaderId) implements DomainEvent {
}
