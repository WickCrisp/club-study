package com.crisp.demo.shared.event;

/**
 * 活动被删除
 *
 * 发布方：activity 域
 * 订阅方：待办中心（关闭该活动相关的待审核待办）、registration（清理报名缓存）
 */
public record ActivityDeleted(Long activityId, Long clubId) implements DomainEvent {
}
