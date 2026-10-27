USE club_system;

-- ============================================
-- 阶段 1 迁移：活动参与范围
-- PUBLIC      公开活动：所有登录用户都可报名
-- MEMBER_ONLY 本社团专属：仅该社团 APPROVED 成员可报名
-- ============================================
ALTER TABLE `activity`
    ADD COLUMN `join_scope` VARCHAR(20) NOT NULL DEFAULT 'PUBLIC'
        COMMENT '参与范围：PUBLIC公开活动 MEMBER_ONLY本社团专属'
        AFTER `status`;

SELECT id, title, status, join_scope FROM `activity` ORDER BY id;
