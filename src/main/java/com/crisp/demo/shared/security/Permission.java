package com.crisp.demo.shared.security;

import com.crisp.demo.shared.error.BusinessException;
import com.crisp.demo.shared.error.ForbiddenException;
import com.crisp.demo.domain.identity.model.User;

/**
 * 统一的权限判定工具
 * 约定：ADMIN 拥有全部权限；非管理员只能操作自己名下的资源（社团负责人 / 活动发布人 / 总结创建人）
 */
public final class Permission {

    private Permission() {
    }

    /** 是否为管理员 */
    public static boolean isAdmin(User user) {
        return user != null && "ADMIN".equals(user.getRole());
    }

    /** 管理员，或 ownerId 所代表的资源归属人本人 */
    public static boolean isAdminOrOwner(User user, Long ownerId) {
        if (user == null) {
            return false;
        }
        return isAdmin(user) || (ownerId != null && ownerId.equals(user.getId()));
    }

    /** 要求管理员身份 */
    public static void requireAdmin(User user, String action) {
        if (!isAdmin(user)) {
            throw new ForbiddenException("无权限：" + action + "仅管理员可操作");
        }
    }

    /** 要求管理员或资源归属人 */
    public static void requireAdminOrOwner(User user, Long ownerId, String action) {
        if (!isAdminOrOwner(user, ownerId)) {
            throw new ForbiddenException("无权限：" + action + "仅资源归属人或管理员可操作");
        }
    }
}
