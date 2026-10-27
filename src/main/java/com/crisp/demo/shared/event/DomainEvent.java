package com.crisp.demo.shared.event;

/**
 * 领域事件标记接口
 *
 * 约定：
 * 1. 事件由 **数据拥有者** 在业务动作完成后发布
 * 2. 订阅方通过 {@code @TransactionalEventListener(phase = AFTER_COMMIT)} 处理
 * 3. 订阅方必须幂等；事件只用于「副作用」（缓存失效、通知、统计），不用于强一致业务状态
 */
public interface DomainEvent {
}
