USE club_system;

-- ============================================
-- 阶段 C 迁移：待办中心
-- 统一承载 5 类审批：社团创建 / 入社 / 退社 / 取消报名 / 活动审核
-- ============================================
CREATE TABLE IF NOT EXISTS `approval_task` (
    `id`            BIGINT       AUTO_INCREMENT PRIMARY KEY,
    `type`          VARCHAR(30)  NOT NULL            COMMENT 'CLUB_CREATE/JOIN/QUIT/CANCEL_REGISTRATION/ACTIVITY_REVIEW',
    `target_type`   VARCHAR(30)  NOT NULL            COMMENT '目标对象类型',
    `target_id`     BIGINT       NOT NULL            COMMENT '目标对象ID',
    `club_id`       BIGINT       DEFAULT NULL        COMMENT '所属社团（用于范围过滤）',
    `applicant_id`  BIGINT       NOT NULL            COMMENT '申请人',
    `approver_scope` VARCHAR(20) NOT NULL            COMMENT '审批人范围：ADMIN 或 USER',
    `approver_id`   BIGINT       DEFAULT NULL        COMMENT '当 scope=USER 时的审批人（如社团负责人）',
    `title`         VARCHAR(200) NOT NULL            COMMENT '待办标题（冗余，便于列表展示）',
    `summary`       VARCHAR(500) DEFAULT NULL        COMMENT '摘要',
    `status`        VARCHAR(20)  NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/APPROVED/REJECTED/CANCELLED',
    `decided_by`    BIGINT       DEFAULT NULL        COMMENT '决策人',
    `decided_at`    DATETIME     DEFAULT NULL        COMMENT '决策时间',
    `remark`        VARCHAR(500) DEFAULT NULL        COMMENT '审批意见',
    `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_approval_status` (`status`),
    INDEX `idx_approval_type` (`type`),
    INDEX `idx_approval_club` (`club_id`),
    INDEX `idx_approval_applicant` (`applicant_id`),
    INDEX `idx_approval_target` (`type`, `target_id`),
    CONSTRAINT `fk_approval_applicant` FOREIGN KEY (`applicant_id`) REFERENCES `user`(`id`)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='待办中心审批任务';

SELECT table_name AS tables_now FROM information_schema.tables
WHERE table_schema = 'club_system' ORDER BY table_name;
