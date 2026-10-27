package com.crisp.demo.shared.event;

/**
 * 报名被取消（直接取消或审批通过）
 *
 * 发布方：registration 域
 * 订阅方：待办中心（关闭取消报名待办，避免孤儿待办）
 */
public record RegistrationCancelled(Long registrationId, Long activityId, Long userId) implements DomainEvent {
}
