CREATE DATABASE IF NOT EXISTS `club_system`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

USE `club_system`;

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

-- 测试用户（密码：Yang@2026 的 MD5）
INSERT INTO `user` (`id`, `student_no`, `password`, `name`, `phone`, `email`, `gender`, `role`) VALUES
(1, '202335710340', 'dfc28e98d88ac4befe4e9115fe71eb1f', '杨铭',   '13800000040', '202335710340@stu.edu.cn', 1, 'ADMIN'),
(2, '202335710341', 'dfc28e98d88ac4befe4e9115fe71eb1f', '李思远', '13800000041', '202335710341@stu.edu.cn', 1, 'STUDENT'),
(3, '202335710342', 'dfc28e98d88ac4befe4e9115fe71eb1f', '王雨桐', '13800000042', '202335710342@stu.edu.cn', 2, 'STUDENT'),
(4, '202335710343', 'dfc28e98d88ac4befe4e9115fe71eb1f', '陈子豪', '13800000043', '202335710343@stu.edu.cn', 1, 'STUDENT'),
(5, '202335710344', 'dfc28e98d88ac4befe4e9115fe71eb1f', '张若曦', '13800000044', '202335710344@stu.edu.cn', 2, 'STUDENT');

-- 社团（负责人均来自上面的测试用户）
INSERT INTO `club` (`id`, `name`, `description`, `category_id`, `leader_id`, `status`) VALUES
(1, '算法竞赛社',     '算法训练、编程竞赛、项目实战',       1, 2, 'ACTIVE'),
(2, '篮球社',         '每周训练、校际比赛、球技切磋',       2, 3, 'ACTIVE'),
(3, '音乐社',         '乐队排练、校园演出、乐器教学',       3, 4, 'ACTIVE'),
(4, '青年志愿者协会', '社区服务、支教活动、环保公益',       4, 5, 'ACTIVE');

-- 社团成员：负责人同时是 LEADER/APPROVED；另有 1 条待审批入社申请用于演示待办中心
INSERT INTO `club_member` (`id`, `user_id`, `club_id`, `role`, `status`, `apply_reason`, `joined_at`) VALUES
(1, 2, 1, 'LEADER', 'APPROVED', NULL,            NOW()),
(2, 3, 2, 'LEADER', 'APPROVED', NULL,            NOW()),
(3, 4, 3, 'LEADER', 'APPROVED', NULL,            NOW()),
(4, 5, 4, 'LEADER', 'APPROVED', NULL,            NOW()),
(5, 3, 1, 'MEMBER', 'APPROVED', '对算法竞赛很感兴趣', NOW()),
(6, 4, 1, 'MEMBER', 'PENDING',  '想加入一起刷题',      NULL);

-- 活动（覆盖：进行中 / 待审核 / 已结束 / 草稿）
INSERT INTO `activity` (`id`, `club_id`, `title`, `description`, `location`, `start_time`, `end_time`, `max_participants`, `current_count`, `status`, `join_scope`, `creator_id`) VALUES
(1, 1, '算法竞赛训练营', '为期一周的算法集训，备战校际编程大赛',   '实验楼A301',     '2026-12-01 09:00:00', '2026-12-01 17:00:00', 30,  1, 'ONGOING',  'MEMBER_ONLY', 2),
(2, 2, '新生篮球友谊赛', '新生队 vs 老生队对抗赛',               '体育馆篮球场',   '2026-12-05 15:00:00', '2026-12-05 18:00:00', 20,  0, 'PENDING',  'PUBLIC',      3),
(3, 3, '校园音乐节',     '多支乐队现场演出，欢迎围观',           '大学生活动中心', '2026-11-10 18:00:00', '2026-11-10 22:00:00', 200, 1, 'FINISHED', 'PUBLIC',      4),
(4, 4, '社区支教活动',   '前往周边小学开展支教',                 '希望小学',       '2026-12-15 08:00:00', '2026-12-15 16:00:00', 25,  1, 'ONGOING',  'MEMBER_ONLY', 5),
(5, 1, '编程马拉松',     '24 小时编程挑战赛，组队参加',          '实验楼B201',     '2027-01-10 09:00:00', '2027-01-11 09:00:00', 40,  0, 'DRAFT',    'PUBLIC',      2);

-- 报名记录（current_count 与登记一致：活动1=1、活动3=1、活动4=1）
INSERT INTO `activity_registration` (`id`, `activity_id`, `user_id`, `status`) VALUES
(1, 1, 3, 'REGISTERED'),
(2, 3, 2, 'REGISTERED'),
(3, 4, 2, 'CANCEL_PENDING');

-- 待办中心（2 条待处理，登录后即可在待办中心操作）
INSERT INTO `approval_task` (`id`, `type`, `target_type`, `target_id`, `club_id`, `applicant_id`, `approver_scope`, `approver_id`, `title`, `summary`, `status`) VALUES
(1, 'JOIN',            'CLUB_MEMBER',  6, 1, 4, 'USER',  2,    '入社申请：算法竞赛社', '申请人：陈子豪', 'PENDING'),
(2, 'ACTIVITY_REVIEW', 'ACTIVITY',     2, 2, 3, 'ADMIN', NULL, '活动审核：新生篮球友谊赛', '发布人：王雨桐，参与范围：PUBLIC', 'PENDING');

-- 全校公告
INSERT INTO `announcement` (`title`, `content`, `pinned`, `status`, `publisher_id`, `published_at`) VALUES
('关于 2026 年秋季社团招新的通知', '各社团招新将于下周一开始，请有意向的同学关注社团风采页并提交入社申请。', 1, 'PUBLISHED', 1, NOW()),
('社团活动审核规则说明', '所有活动须经管理员审核通过后方可开放报名，请各社团负责人提前 3 天提交审核。', 0, 'PUBLISHED', 1, NOW());

-- 社团风采（一个社团可有多条图文）
INSERT INTO `club_showcase` (`club_id`, `title`, `content`, `sort`, `status`, `creator_id`) VALUES
(1, '算法竞赛训练营精彩回顾', '为期一周的集训，社员们在实验楼 A301 完成了 12 场模拟赛。', 10, 'PUBLISHED', 2),
(1, '我们的实验室日常',       '每周三晚的固定训练时间，欢迎新同学加入。',               20, 'PUBLISHED', 2),
(3, '校园音乐节现场',         '六支乐队轮番登场，现场观众约 300 人。',                 10, 'PUBLISHED', 4);
