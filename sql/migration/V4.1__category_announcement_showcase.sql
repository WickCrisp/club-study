USE club_system;

-- ============================================
-- 阶段 D 迁移：分类 / 公告 / 社团风采
-- ============================================

-- 1. 社团分类
CREATE TABLE IF NOT EXISTS `club_category` (
    `id`         BIGINT       AUTO_INCREMENT PRIMARY KEY,
    `name`       VARCHAR(50)  NOT NULL UNIQUE COMMENT '分类名称',
    `sort`       INT          NOT NULL DEFAULT 0 COMMENT '排序值，越小越靠前',
    `status`     VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE/DISABLED',
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='社团分类';

-- 2. 社团表增加分类外键
ALTER TABLE `club`
    ADD COLUMN `category_id` BIGINT DEFAULT NULL COMMENT '所属分类' AFTER `description`,
    ADD INDEX `idx_club_category` (`category_id`),
    ADD CONSTRAINT `fk_club_category` FOREIGN KEY (`category_id`) REFERENCES `club_category`(`id`)
        ON DELETE SET NULL ON UPDATE CASCADE;

-- 3. 全校公告
CREATE TABLE IF NOT EXISTS `announcement` (
    `id`           BIGINT       AUTO_INCREMENT PRIMARY KEY,
    `title`        VARCHAR(200) NOT NULL COMMENT '标题',
    `content`      TEXT         NOT NULL COMMENT '正文',
    `pinned`       TINYINT      NOT NULL DEFAULT 0 COMMENT '是否置顶',
    `status`       VARCHAR(20)  NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT/PUBLISHED',
    `publisher_id` BIGINT       NOT NULL COMMENT '发布人',
    `published_at` DATETIME     DEFAULT NULL COMMENT '发布时间',
    `created_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_announcement_status` (`status`),
    INDEX `idx_announcement_pinned` (`pinned`),
    CONSTRAINT `fk_announcement_publisher` FOREIGN KEY (`publisher_id`) REFERENCES `user`(`id`)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='全校公告';

-- 4. 社团风采（一个社团可有多条图文）
CREATE TABLE IF NOT EXISTS `club_showcase` (
    `id`         BIGINT        AUTO_INCREMENT PRIMARY KEY,
    `club_id`    BIGINT        NOT NULL COMMENT '所属社团',
    `title`      VARCHAR(200)  NOT NULL COMMENT '标题',
    `content`    TEXT          DEFAULT NULL COMMENT '图文内容',
    `cover_url`  VARCHAR(500)  DEFAULT NULL COMMENT '封面图',
    `images`     VARCHAR(2000) DEFAULT NULL COMMENT '图片URL，逗号分隔',
    `sort`       INT           NOT NULL DEFAULT 0 COMMENT '排序值',
    `status`     VARCHAR(20)   NOT NULL DEFAULT 'PUBLISHED' COMMENT 'DRAFT/PUBLISHED',
    `creator_id` BIGINT        NOT NULL COMMENT '创建人',
    `created_at` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_showcase_club` (`club_id`),
    INDEX `idx_showcase_status` (`status`),
    CONSTRAINT `fk_showcase_club` FOREIGN KEY (`club_id`) REFERENCES `club`(`id`)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_showcase_creator` FOREIGN KEY (`creator_id`) REFERENCES `user`(`id`)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='社团风采图文';

-- 5. 演示分类
INSERT INTO `club_category` (`name`, `sort`) VALUES
('学术科技', 10), ('体育运动', 20), ('文艺表演', 30), ('志愿公益', 40), ('兴趣爱好', 50);

SELECT table_name AS tables_now FROM information_schema.tables
WHERE table_schema = 'club_system' ORDER BY table_name;
