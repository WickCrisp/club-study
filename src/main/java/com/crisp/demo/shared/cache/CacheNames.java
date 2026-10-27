package com.crisp.demo.shared.cache;

/**
 * 缓存命名空间 — 前缀即域，只有该域的组件才允许读写对应前缀
 *
 * club:*     归 club 域
 * activity:* 归 activity 域
 *
 * 跨域变更一律通过领域事件通知「拥有者」自己去清，禁止越权清别人的缓存。
 */
public final class CacheNames {

    private CacheNames() {
    }

    /** 社团详情（key = 社团 id） */
    public static final String CLUB_DETAIL = "club:detail";
    /** 社团列表 / 搜索 / 我管理的社团 */
    public static final String CLUB_LIST = "club:list";

    /** 活动详情（key = 活动 id） */
    public static final String ACTIVITY_DETAIL = "activity:detail";
    /** 活动列表 / 搜索 / 按社团·状态筛选 */
    public static final String ACTIVITY_LIST = "activity:list";
}
