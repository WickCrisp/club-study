package com.crisp.demo.domain.activity.api;

/**
 * 活动名额端口 — registration 域（名额的唯一写者）通过它调整 current_count
 *
 * activity 域只提供原子操作，不关心业务语义：
 *   occupy  → current_count + 1（受人数上限约束），成功返回 true
 *   release → current_count - 1（不小于 0），成功返回 true
 *
 * 这样「谁拥有数据、谁提供端口」与「谁执行业务、谁调用端口」分离。
 */
public interface ActivityOccupancyPort {

    boolean occupy(Long activityId);

    boolean release(Long activityId);
}
