package com.crisp.demo.shared.event;

/**
 * 社团被删除
 *
 * 发布方：club 域
 * 订阅方：所有因数据库级联而被连带删除数据的域（activity 等），各自清理自己的缓存
 */
public record ClubDeleted(Long clubId) implements DomainEvent {
}
