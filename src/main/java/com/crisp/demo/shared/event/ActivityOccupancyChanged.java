package com.crisp.demo.shared.event;

/**
 * 活动名额发生变化（报名 / 取消 / 取消审批）
 *
 * 发布方：registration 域（它拥有 current_count 的写权限）
 * 订阅方：activity 域（清除自己的缓存，读到新人数）
 */
public record ActivityOccupancyChanged(Long activityId) implements DomainEvent {
}
