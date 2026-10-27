package com.crisp.demo.shared.event;

/**
 * 用户被删除
 *
 * 发布方：identity 域
 * 订阅方：club（其负责的社团 leader_id 被置空）、activity（其发布的活动被级联删除），各自清理缓存
 */
public record UserDeleted(Long userId) implements DomainEvent {
}
