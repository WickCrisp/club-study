-- ============================================
-- 社团活动管理系统 - 数据库初始化脚本
-- 数据库名: club_system
-- 字符集: utf8mb4
-- ============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `club_system`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

USE `club_system`;

-- ============================================
-- 1. 用户信息表
-- ============================================
CREATE TABLE IF NOT EXISTS `user` (
    `id`            BIGINT          AUTO_INCREMENT PRIMARY KEY,
    `student_no`    VARCHAR(20)     NOT NULL UNIQUE   COMMENT '学号，登录账号',
    `password`      VARCHAR(255)    NOT NULL          COMMENT '加密后的密码',
    `name`          VARCHAR(50)     NOT NULL          COMMENT '真实姓名',
    `phone`         VARCHAR(20)     DEFAULT NULL      COMMENT '联系电话',
    `email`         VARCHAR(100)    DEFAULT NULL      COMMENT '电子邮箱',
    `gender`        TINYINT         DEFAULT 0         COMMENT '性别：0未知 1男 2女',
    `role`          VARCHAR(20)     NOT NULL DEFAULT 'STUDENT' COMMENT '角色：STUDENT学生 ADMIN管理员',
    `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户信息表';

-- ============================================
-- 2. 社团信息表
-- ============================================
CREATE TABLE IF NOT EXISTS `club` (
    `id`            BIGINT          AUTO_INCREMENT PRIMARY KEY,
    `name`          VARCHAR(100)    NOT NULL UNIQUE   COMMENT '社团名称',
    `description`   TEXT            DEFAULT NULL      COMMENT '社团简介',
    `logo_url`      VARCHAR(255)    DEFAULT NULL      COMMENT '社团Logo图片地址',
    `leader_id`     BIGINT          DEFAULT NULL      COMMENT '负责人ID，关联user表',
    `status`        VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE活跃 DISBANDED解散',
    `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_club_leader` (`leader_id`),
    CONSTRAINT `fk_club_leader` FOREIGN KEY (`leader_id`) REFERENCES `user`(`id`)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='社团信息表';

-- ============================================
-- 3. 社团成员表
-- ============================================
CREATE TABLE IF NOT EXISTS `club_member` (
    `id`            BIGINT          AUTO_INCREMENT PRIMARY KEY,
    `user_id`       BIGINT          NOT NULL            COMMENT '学生ID',
    `club_id`       BIGINT          NOT NULL            COMMENT '社团ID',
    `role`          VARCHAR(20)     NOT NULL DEFAULT 'MEMBER' COMMENT '成员角色：LEADER负责人 VICE_LEADER副负责人 MEMBER普通成员',
    `status`        VARCHAR(20)     NOT NULL DEFAULT 'PENDING' COMMENT '审核状态：PENDING待审核 APPROVED已通过 REJECTED已拒绝',
    `apply_reason`  VARCHAR(500)    DEFAULT NULL        COMMENT '申请理由',
    `joined_at`     DATETIME        DEFAULT NULL        COMMENT '加入时间（审核通过后填入）',
    `quit_at`       DATETIME        DEFAULT NULL        COMMENT '退出时间',
    `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_member_user` (`user_id`),
    INDEX `idx_member_club` (`club_id`),
    UNIQUE KEY `uk_user_club` (`user_id`, `club_id`),
    CONSTRAINT `fk_member_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_member_club` FOREIGN KEY (`club_id`) REFERENCES `club`(`id`)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='社团成员表';

-- ============================================
-- 4. 活动信息表
-- ============================================
CREATE TABLE IF NOT EXISTS `activity` (
    `id`                BIGINT          AUTO_INCREMENT PRIMARY KEY,
    `club_id`           BIGINT          NOT NULL            COMMENT '所属社团ID',
    `title`             VARCHAR(200)    NOT NULL            COMMENT '活动名称',
    `description`       TEXT            DEFAULT NULL        COMMENT '活动简介',
    `location`          VARCHAR(200)    NOT NULL            COMMENT '活动地点',
    `start_time`        DATETIME        NOT NULL            COMMENT '活动开始时间',
    `end_time`          DATETIME        NOT NULL            COMMENT '活动结束时间',
    `max_participants`  INT             NOT NULL DEFAULT 0  COMMENT '人数限制（0表示不限制）',
    `current_count`     INT             NOT NULL DEFAULT 0  COMMENT '当前已报名人数',
    `status`            VARCHAR(20)     NOT NULL DEFAULT 'DRAFT' COMMENT '状态：DRAFT草稿 PUBLISHED已发布 ONGOING进行中 FINISHED已结束 CANCELLED已取消',
    `creator_id`        BIGINT          NOT NULL            COMMENT '发布人ID',
    `created_at`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_activity_club` (`club_id`),
    INDEX `idx_activity_status` (`status`),
    INDEX `idx_activity_start_time` (`start_time`),
    CONSTRAINT `fk_activity_club` FOREIGN KEY (`club_id`) REFERENCES `club`(`id`)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_activity_creator` FOREIGN KEY (`creator_id`) REFERENCES `user`(`id`)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动信息表';

-- ============================================
-- 5. 活动报名表
-- ============================================
CREATE TABLE IF NOT EXISTS `activity_registration` (
    `id`            BIGINT      AUTO_INCREMENT PRIMARY KEY,
    `activity_id`   BIGINT      NOT NULL                COMMENT '活动ID',
    `user_id`       BIGINT      NOT NULL                COMMENT '报名学生ID',
    `status`        VARCHAR(20) NOT NULL DEFAULT 'REGISTERED' COMMENT '状态：REGISTERED已报名 CANCELLED已取消',
    `registered_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '报名时间',
    `cancelled_at`  DATETIME    DEFAULT NULL            COMMENT '取消时间',
    INDEX `idx_reg_activity` (`activity_id`),
    INDEX `idx_reg_user` (`user_id`),
    UNIQUE KEY `uk_activity_user` (`activity_id`, `user_id`),
    CONSTRAINT `fk_reg_activity` FOREIGN KEY (`activity_id`) REFERENCES `activity`(`id`)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_reg_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动报名表';

-- ============================================
-- 6. 活动签到签退表
-- ============================================
CREATE TABLE IF NOT EXISTS `activity_checkin` (
    `id`                BIGINT          AUTO_INCREMENT PRIMARY KEY,
    `registration_id`   BIGINT          NOT NULL            COMMENT '报名记录ID',
    `activity_id`       BIGINT          NOT NULL            COMMENT '活动ID（冗余）',
    `user_id`           BIGINT          NOT NULL            COMMENT '学生ID（冗余）',
    `checkin_time`      DATETIME        DEFAULT NULL        COMMENT '签到时间',
    `checkin_status`    VARCHAR(20)     NOT NULL DEFAULT 'UNCHECKED' COMMENT '签到状态：UNCHECKED未签到 CHECKED已签到',
    `checkout_time`     DATETIME        DEFAULT NULL        COMMENT '签退时间',
    `checkout_status`   VARCHAR(20)     NOT NULL DEFAULT 'UNCHECKED' COMMENT '签退状态：UNCHECKED未签退 CHECKED已签退',
    `created_at`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_checkin_activity` (`activity_id`),
    INDEX `idx_checkin_user` (`user_id`),
    UNIQUE KEY `uk_checkin_registration` (`registration_id`),
    CONSTRAINT `fk_checkin_reg` FOREIGN KEY (`registration_id`) REFERENCES `activity_registration`(`id`)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_checkin_activity` FOREIGN KEY (`activity_id`) REFERENCES `activity`(`id`)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_checkin_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动签到签退表';

-- ============================================
-- 7. 活动总结表
-- ============================================
CREATE TABLE IF NOT EXISTS `activity_summary` (
    `id`            BIGINT          AUTO_INCREMENT PRIMARY KEY,
    `activity_id`   BIGINT          NOT NULL            COMMENT '活动ID',
    `content`       TEXT            NOT NULL            COMMENT '活动内容总结',
    `effect`        TEXT            DEFAULT NULL        COMMENT '活动效果评估',
    `suggestions`   TEXT            DEFAULT NULL        COMMENT '改进建议',
    `images`        VARCHAR(2000)   DEFAULT NULL        COMMENT '活动照片URL，多个用逗号分隔',
    `creator_id`    BIGINT          NOT NULL            COMMENT '总结创建人ID',
    `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_summary_activity` (`activity_id`),
    INDEX `idx_summary_creator` (`creator_id`),
    CONSTRAINT `fk_summary_activity` FOREIGN KEY (`activity_id`) REFERENCES `activity`(`id`)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_summary_creator` FOREIGN KEY (`creator_id`) REFERENCES `user`(`id`)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动总结表';

-- ============================================
-- 插入测试数据（可选）
-- ============================================

-- 管理员账号：admin / 密码：123456（MD5: e10adc3949ba59abbe56e057f20f883e）
INSERT INTO `user` (`student_no`, `password`, `name`, `role`) VALUES
('admin', 'e10adc3949ba59abbe56e057f20f883e', '系统管理员', 'ADMIN');

-- 测试学生账号：2024001 / 密码：123456
INSERT INTO `user` (`student_no`, `password`, `name`, `phone`, `gender`, `role`) VALUES
('2024001', 'e10adc3949ba59abbe56e057f20f883e', '张三', '13800001111', 1, 'STUDENT'),
('2024002', 'e10adc3949ba59abbe56e057f20f883e', '李四', '13800002222', 2, 'STUDENT');
