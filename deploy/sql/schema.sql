
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

-- ============================================
-- 1. 用户信息表
-- ============================================
CREATE TABLE `user` (
    `id`            BIGINT          AUTO_INCREMENT PRIMARY KEY,
    `student_no`    VARCHAR(20)     NOT NULL UNIQUE   COMMENT '学号，登录账号',
    `password`      VARCHAR(255)    NOT NULL          COMMENT 'MD5 加密后的密码',
    `name`          VARCHAR(50)     NOT NULL          COMMENT '真实姓名',
    `phone`         VARCHAR(20)     DEFAULT NULL      COMMENT '联系电话',
    `email`         VARCHAR(100)    DEFAULT NULL      COMMENT '电子邮箱',
    `gender`        TINYINT         DEFAULT 0         COMMENT '性别：0未知 1男 2女',
    `role`          VARCHAR(20)     NOT NULL DEFAULT 'STUDENT' COMMENT '角色：STUDENT学生 ADMIN管理员',
    `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户信息表';

-- ============================================
-- 2. 社团分类表
-- ============================================
CREATE TABLE `club_category` (
    `id`         BIGINT       AUTO_INCREMENT PRIMARY KEY,
    `name`       VARCHAR(50)  NOT NULL UNIQUE COMMENT '分类名称',
    `sort`       INT          NOT NULL DEFAULT 0 COMMENT '排序值',
    `status`     VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE/DISABLED',
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='社团分类';

-- ============================================
-- 3. 社团信息表
-- ============================================
CREATE TABLE `club` (
    `id`            BIGINT          AUTO_INCREMENT PRIMARY KEY,
    `name`          VARCHAR(100)    NOT NULL UNIQUE   COMMENT '社团名称',
    `description`   TEXT            DEFAULT NULL      COMMENT '社团简介',
    `category_id`   BIGINT          DEFAULT NULL      COMMENT '所属分类',
    `leader_id`     BIGINT          DEFAULT NULL      COMMENT '负责人ID，关联user表',
    `status`        VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE活跃 DISBANDED解散',
    `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_club_leader` (`leader_id`),
    INDEX `idx_club_category` (`category_id`),
    CONSTRAINT `fk_club_category` FOREIGN KEY (`category_id`) REFERENCES `club_category`(`id`)
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT `fk_club_leader` FOREIGN KEY (`leader_id`) REFERENCES `user`(`id`)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='社团信息表';

-- ============================================
-- 3. 社团成员表
-- ============================================
CREATE TABLE `club_member` (
    `id`            BIGINT          AUTO_INCREMENT PRIMARY KEY,
    `user_id`       BIGINT          NOT NULL            COMMENT '学生ID',
    `club_id`       BIGINT          NOT NULL            COMMENT '社团ID',
    `role`          VARCHAR(20)     NOT NULL DEFAULT 'MEMBER' COMMENT '成员角色：LEADER负责人 VICE_LEADER副负责人 MEMBER普通成员',
    `status`        VARCHAR(20)     NOT NULL DEFAULT 'PENDING' COMMENT '审核状态：PENDING待审核 APPROVED已通过 REJECTED已拒绝 QUIT_PENDING退出待审 QUIT已退出',
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
CREATE TABLE `activity` (
    `id`                BIGINT          AUTO_INCREMENT PRIMARY KEY,
    `club_id`           BIGINT          NOT NULL            COMMENT '所属社团ID',
    `title`             VARCHAR(200)    NOT NULL            COMMENT '活动名称',
    `description`       TEXT            DEFAULT NULL        COMMENT '活动简介',
    `location`          VARCHAR(200)    NOT NULL            COMMENT '活动地点',
    `start_time`        DATETIME        NOT NULL            COMMENT '活动开始时间',
    `end_time`          DATETIME        NOT NULL            COMMENT '活动结束时间',
    `max_participants`  INT             NOT NULL DEFAULT 0  COMMENT '人数限制（0表示不限制）',
    `current_count`     INT             NOT NULL DEFAULT 0  COMMENT '当前已报名人数',
    `status`            VARCHAR(20)     NOT NULL DEFAULT 'DRAFT' COMMENT '状态：DRAFT草稿 ONGOING已发布/进行中 FINISHED已结束 CANCELLED已取消',
    `join_scope`        VARCHAR(20)     NOT NULL DEFAULT 'PUBLIC' COMMENT '参与范围：PUBLIC公开活动 MEMBER_ONLY本社团专属',
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
CREATE TABLE `activity_registration` (
    `id`            BIGINT      AUTO_INCREMENT PRIMARY KEY,
    `activity_id`   BIGINT      NOT NULL                COMMENT '活动ID',
    `user_id`       BIGINT      NOT NULL                COMMENT '报名学生ID',
    `status`        VARCHAR(20) NOT NULL DEFAULT 'REGISTERED' COMMENT '状态：REGISTERED已报名 CANCEL_PENDING取消待审 CANCELLED已取消',
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
-- 6. 待办中心（五类审批）
-- ============================================
CREATE TABLE `approval_task` (
    `id`             BIGINT       AUTO_INCREMENT PRIMARY KEY,
    `type`           VARCHAR(30)  NOT NULL            COMMENT 'CLUB_CREATE/JOIN/QUIT/CANCEL_REGISTRATION/ACTIVITY_REVIEW',
    `target_type`    VARCHAR(30)  NOT NULL            COMMENT '目标对象类型',
    `target_id`      BIGINT       NOT NULL            COMMENT '目标对象ID',
    `club_id`        BIGINT       DEFAULT NULL        COMMENT '所属社团',
    `applicant_id`   BIGINT       NOT NULL            COMMENT '申请人',
    `approver_scope` VARCHAR(20)  NOT NULL            COMMENT 'ADMIN 或 USER',
    `approver_id`    BIGINT       DEFAULT NULL        COMMENT 'scope=USER 时的审批人',
    `title`          VARCHAR(200) NOT NULL            COMMENT '待办标题',
    `summary`        VARCHAR(500) DEFAULT NULL        COMMENT '摘要',
    `status`         VARCHAR(20)  NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/APPROVED/REJECTED/CANCELLED',
    `decided_by`     BIGINT       DEFAULT NULL        COMMENT '决策人',
    `decided_at`     DATETIME     DEFAULT NULL        COMMENT '决策时间',
    `remark`         VARCHAR(500) DEFAULT NULL        COMMENT '审批意见',
    `created_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_approval_status` (`status`),
    INDEX `idx_approval_type` (`type`),
    INDEX `idx_approval_club` (`club_id`),
    INDEX `idx_approval_applicant` (`applicant_id`),
    INDEX `idx_approval_target` (`type`, `target_id`),
    CONSTRAINT `fk_approval_applicant` FOREIGN KEY (`applicant_id`) REFERENCES `user`(`id`)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='待办中心审批任务';

-- ============================================
-- 7. 全校公告
-- ============================================
CREATE TABLE `announcement` (
    `id`           BIGINT       AUTO_INCREMENT PRIMARY KEY,
    `title`        VARCHAR(200) NOT NULL            COMMENT '标题',
    `content`      TEXT         NOT NULL            COMMENT '正文',
    `pinned`       TINYINT      NOT NULL DEFAULT 0  COMMENT '是否置顶',
    `status`       VARCHAR(20)  NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT/PUBLISHED',
    `publisher_id` BIGINT       NOT NULL            COMMENT '发布人',
    `published_at` DATETIME     DEFAULT NULL        COMMENT '发布时间',
    `created_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_announcement_status` (`status`),
    INDEX `idx_announcement_pinned` (`pinned`),
    CONSTRAINT `fk_announcement_publisher` FOREIGN KEY (`publisher_id`) REFERENCES `user`(`id`)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='全校公告';

-- ============================================
-- 8. 社团风采（一个社团可有多条图文）
-- ============================================
CREATE TABLE `club_showcase` (
    `id`         BIGINT        AUTO_INCREMENT PRIMARY KEY,
    `club_id`    BIGINT        NOT NULL            COMMENT '所属社团',
    `title`      VARCHAR(200)  NOT NULL            COMMENT '标题',
    `content`    TEXT          DEFAULT NULL        COMMENT '图文内容',
    `cover_url`  VARCHAR(500)  DEFAULT NULL        COMMENT '封面图',
    `images`     VARCHAR(2000) DEFAULT NULL        COMMENT '图片URL，逗号分隔',
    `sort`       INT           NOT NULL DEFAULT 0  COMMENT '排序值',
    `status`     VARCHAR(20)   NOT NULL DEFAULT 'PUBLISHED' COMMENT 'DRAFT/PUBLISHED',
    `creator_id` BIGINT        NOT NULL            COMMENT '创建人',
    `created_at` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_showcase_club` (`club_id`),
    INDEX `idx_showcase_status` (`status`),
    CONSTRAINT `fk_showcase_club` FOREIGN KEY (`club_id`) REFERENCES `club`(`id`)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_showcase_creator` FOREIGN KEY (`creator_id`) REFERENCES `user`(`id`)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='社团风采图文';

-- ============================================
-- 演示数据（统一使用「12 位学号 + Yang@2026」测试账号）
--
-- 测试账号一览（密码全部为 Yang@2026）：
--   202335710340  杨铭     管理员
--   202335710341  李思远   学生 / 算法竞赛社社长
--   202335710342  王雨桐   学生 / 篮球社社长
--   202335710343  陈子豪   学生 / 音乐社社长
--   202335710344  张若曦   学生 / 青年志愿者协会社长
-- ============================================

-- 社团分类
INSERT INTO `club_category` (`id`, `name`, `sort`, `status`) VALUES
(1, '学术科技', 10, 'ACTIVE'),
(2, '体育运动', 20, 'ACTIVE'),
(3, '文艺表演', 30, 'ACTIVE'),
(4, '志愿公益', 40, 'ACTIVE'),
(5, '兴趣爱好', 50, 'ACTIVE');


-- ============================================
-- 初始管理员（生产部署引导账号）
-- 账号 admin / 初始密码 Admin@2026 —— 首次登录后必须立即修改！
-- ============================================
INSERT INTO `user` (`student_no`, `password`, `name`, `phone`, `email`, `gender`, `role`) VALUES
('admin', '$2a$10$3zlTTT5u9MnY3gPlWRTnoO/7novzT1186cXJsmuB79zf0Xj/Gl8Pm', '管理员', NULL, NULL, 1, 'ADMIN');
