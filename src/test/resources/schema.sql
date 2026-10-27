-- ============================================
-- 测试库结构（由 spring.sql.init 在测试上下文启动时执行）
-- 目标库：club_system_test（与开发库 club_system 完全隔离）
-- 只建表、不写数据，测试数据由 TestFixtures 自行准备
-- ============================================

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS `club_showcase`;
DROP TABLE IF EXISTS `announcement`;
DROP TABLE IF EXISTS `approval_task`;
DROP TABLE IF EXISTS `activity_registration`;
DROP TABLE IF EXISTS `activity`;
DROP TABLE IF EXISTS `club_member`;
DROP TABLE IF EXISTS `club`;
DROP TABLE IF EXISTS `club_category`;
DROP TABLE IF EXISTS `user`;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE `user` (
    `id`            BIGINT          AUTO_INCREMENT PRIMARY KEY,
    `student_no`    VARCHAR(20)     NOT NULL UNIQUE,
    `password`      VARCHAR(255)    NOT NULL,
    `name`          VARCHAR(50)     NOT NULL,
    `phone`         VARCHAR(20)     DEFAULT NULL,
    `email`         VARCHAR(100)    DEFAULT NULL,
    `gender`        TINYINT         DEFAULT 0,
    `role`          VARCHAR(20)     NOT NULL DEFAULT 'STUDENT',
    `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `club_category` (
    `id`         BIGINT       AUTO_INCREMENT PRIMARY KEY,
    `name`       VARCHAR(50)  NOT NULL UNIQUE,
    `sort`       INT          NOT NULL DEFAULT 0,
    `status`     VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `club` (
    `id`            BIGINT          AUTO_INCREMENT PRIMARY KEY,
    `name`          VARCHAR(100)    NOT NULL UNIQUE,
    `description`   TEXT            DEFAULT NULL,
    `category_id`   BIGINT          DEFAULT NULL,
    `leader_id`     BIGINT          DEFAULT NULL,
    `status`        VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_club_leader` (`leader_id`),
    INDEX `idx_club_category` (`category_id`),
    CONSTRAINT `fk_club_category` FOREIGN KEY (`category_id`) REFERENCES `club_category`(`id`)
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT `fk_club_leader` FOREIGN KEY (`leader_id`) REFERENCES `user`(`id`)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `club_member` (
    `id`            BIGINT          AUTO_INCREMENT PRIMARY KEY,
    `user_id`       BIGINT          NOT NULL,
    `club_id`       BIGINT          NOT NULL,
    `role`          VARCHAR(20)     NOT NULL DEFAULT 'MEMBER',
    `status`        VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    `apply_reason`  VARCHAR(500)    DEFAULT NULL,
    `joined_at`     DATETIME        DEFAULT NULL,
    `quit_at`       DATETIME        DEFAULT NULL,
    `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_member_user` (`user_id`),
    INDEX `idx_member_club` (`club_id`),
    UNIQUE KEY `uk_user_club` (`user_id`, `club_id`),
    CONSTRAINT `fk_member_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_member_club` FOREIGN KEY (`club_id`) REFERENCES `club`(`id`)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `activity` (
    `id`                BIGINT          AUTO_INCREMENT PRIMARY KEY,
    `club_id`           BIGINT          NOT NULL,
    `title`             VARCHAR(200)    NOT NULL,
    `description`       TEXT            DEFAULT NULL,
    `location`          VARCHAR(200)    NOT NULL,
    `start_time`        DATETIME        NOT NULL,
    `end_time`          DATETIME        NOT NULL,
    `max_participants`  INT             NOT NULL DEFAULT 0,
    `current_count`     INT             NOT NULL DEFAULT 0,
    `status`            VARCHAR(20)     NOT NULL DEFAULT 'DRAFT',
    `join_scope`        VARCHAR(20)     NOT NULL DEFAULT 'PUBLIC',
    `creator_id`        BIGINT          NOT NULL,
    `created_at`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_activity_club` (`club_id`),
    INDEX `idx_activity_status` (`status`),
    INDEX `idx_activity_start_time` (`start_time`),
    CONSTRAINT `fk_activity_club` FOREIGN KEY (`club_id`) REFERENCES `club`(`id`)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_activity_creator` FOREIGN KEY (`creator_id`) REFERENCES `user`(`id`)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `activity_registration` (
    `id`            BIGINT      AUTO_INCREMENT PRIMARY KEY,
    `activity_id`   BIGINT      NOT NULL,
    `user_id`       BIGINT      NOT NULL,
    `status`        VARCHAR(20) NOT NULL DEFAULT 'REGISTERED',
    `registered_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `cancelled_at`  DATETIME    DEFAULT NULL,
    INDEX `idx_reg_activity` (`activity_id`),
    INDEX `idx_reg_user` (`user_id`),
    UNIQUE KEY `uk_activity_user` (`activity_id`, `user_id`),
    CONSTRAINT `fk_reg_activity` FOREIGN KEY (`activity_id`) REFERENCES `activity`(`id`)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_reg_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `approval_task` (
    `id`             BIGINT       AUTO_INCREMENT PRIMARY KEY,
    `type`           VARCHAR(30)  NOT NULL,
    `target_type`    VARCHAR(30)  NOT NULL,
    `target_id`      BIGINT       NOT NULL,
    `club_id`        BIGINT       DEFAULT NULL,
    `applicant_id`   BIGINT       NOT NULL,
    `approver_scope` VARCHAR(20)  NOT NULL,
    `approver_id`    BIGINT       DEFAULT NULL,
    `title`          VARCHAR(200) NOT NULL,
    `summary`        VARCHAR(500) DEFAULT NULL,
    `status`         VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    `decided_by`     BIGINT       DEFAULT NULL,
    `decided_at`     DATETIME     DEFAULT NULL,
    `remark`         VARCHAR(500) DEFAULT NULL,
    `created_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_approval_status` (`status`),
    INDEX `idx_approval_type` (`type`),
    INDEX `idx_approval_club` (`club_id`),
    INDEX `idx_approval_applicant` (`applicant_id`),
    INDEX `idx_approval_target` (`type`, `target_id`),
    CONSTRAINT `fk_approval_applicant` FOREIGN KEY (`applicant_id`) REFERENCES `user`(`id`)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `announcement` (
    `id`           BIGINT       AUTO_INCREMENT PRIMARY KEY,
    `title`        VARCHAR(200) NOT NULL,
    `content`      TEXT         NOT NULL,
    `pinned`       TINYINT      NOT NULL DEFAULT 0,
    `status`       VARCHAR(20)  NOT NULL DEFAULT 'DRAFT',
    `publisher_id` BIGINT       NOT NULL,
    `published_at` DATETIME     DEFAULT NULL,
    `created_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_announcement_status` (`status`),
    CONSTRAINT `fk_announcement_publisher` FOREIGN KEY (`publisher_id`) REFERENCES `user`(`id`)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `club_showcase` (
    `id`         BIGINT        AUTO_INCREMENT PRIMARY KEY,
    `club_id`    BIGINT        NOT NULL,
    `title`      VARCHAR(200)  NOT NULL,
    `content`    TEXT          DEFAULT NULL,
    `cover_url`  VARCHAR(500)  DEFAULT NULL,
    `images`     VARCHAR(2000) DEFAULT NULL,
    `sort`       INT           NOT NULL DEFAULT 0,
    `status`     VARCHAR(20)   NOT NULL DEFAULT 'PUBLISHED',
    `creator_id` BIGINT        NOT NULL,
    `created_at` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_showcase_club` (`club_id`),
    INDEX `idx_showcase_status` (`status`),
    CONSTRAINT `fk_showcase_club` FOREIGN KEY (`club_id`) REFERENCES `club`(`id`)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_showcase_creator` FOREIGN KEY (`creator_id`) REFERENCES `user`(`id`)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
