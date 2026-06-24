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

-- ============================================
-- 测试学生账号（10 人）
-- ============================================
INSERT INTO `user` (`student_no`, `password`, `name`, `phone`, `gender`, `role`) VALUES
('2024003', 'e10adc3949ba59abbe56e057f20f883e', '王五', '13800003333', 1, 'STUDENT'),
('2024004', 'e10adc3949ba59abbe56e057f20f883e', '赵六', '13800004444', 2, 'STUDENT'),
('2024005', 'e10adc3949ba59abbe56e057f20f883e', '陈七', '13800005555', 1, 'STUDENT'),
('2024006', 'e10adc3949ba59abbe56e057f20f883e', '刘八', '13800006666', 2, 'STUDENT'),
('2024007', 'e10adc3949ba59abbe56e057f20f883e', '周杰', '13800007777', 1, 'STUDENT'),
('2024008', 'e10adc3949ba59abbe56e057f20f883e', '吴磊', '13800008888', 1, 'STUDENT'),
('2024009', 'e10adc3949ba59abbe56e057f20f883e', '郑爽', '13800009999', 2, 'STUDENT'),
('2024010', 'e10adc3949ba59abbe56e057f20f883e', '钱学', '13800001110', 1, 'STUDENT'),
('2024011', 'e10adc3949ba59abbe56e057f20f883e', '孙雨', '13800001112', 2, 'STUDENT'),
('2024012', 'e10adc3949ba59abbe56e057f20f883e', '李华', '13800001113', 1, 'STUDENT');

-- ============================================
-- 测试社团（10 个，admin 为负责人）
-- ============================================
INSERT INTO `club` (`name`, `description`, `leader_id`, `status`) VALUES
('计算机协会', '编程技术交流、算法竞赛、项目实战', 1, 'ACTIVE'),
('篮球社', '每周训练、校际比赛、球技切磋', 1, 'ACTIVE'),
('音乐社', '乐队排练、校园演出、乐器教学', 1, 'ACTIVE'),
('舞蹈社', '街舞、韩舞、现代舞，定期排练与表演', 1, 'ACTIVE'),
('摄影社', '外拍采风、人像摄影、后期教学', 1, 'ACTIVE'),
('志愿者协会', '社区服务、支教活动、环保公益', 1, 'ACTIVE'),
('英语角', '口语练习、英语演讲、文化交流', 1, 'ACTIVE'),
('辩论队', '逻辑训练、模拟辩论、校际联赛', 1, 'ACTIVE'),
('动漫社', '动漫观影、cosplay、绘画创作', 1, 'ACTIVE');

-- ============================================
-- 示例活动（10 个，分布在不同社团）
-- ============================================
INSERT INTO `activity` (`club_id`, `title`, `description`, `location`, `start_time`, `end_time`, `max_participants`, `current_count`, `status`, `creator_id`) VALUES
(13, '算法竞赛训练营', '为期一周的算法集训，备战校际编程大赛', '实验楼A301', '2026-07-01 09:00:00', '2026-07-01 17:00:00', 30, 0, 'ONGOING', 1),
(14, '篮球友谊赛', '计算机学院 vs 管理学院篮球对抗赛', '体育馆篮球场', '2026-07-05 15:00:00', '2026-07-05 18:00:00', 20, 0, 'ONGOING', 1),
(15, '校园音乐节', '夏季校园音乐节，多支乐队现场演出', '大学生活动中心', '2026-07-10 18:00:00', '2026-07-10 22:00:00', 200, 0, 'ONGOING', 1),
(16, '舞蹈快闪活动', '午间舞蹈快闪，展现青春活力', '食堂前广场', '2026-07-03 12:00:00', '2026-07-03 12:30:00', 15, 0, 'FINISHED', 1),
(17, '校园摄影大赛', '主题：夏日校园光影，作品投稿评选', '线上投稿', '2026-07-01 00:00:00', '2026-07-20 23:59:59', 0, 0, 'ONGOING', 1),
(18, '社区支教活动', '前往周边小学开展暑期支教', '希望小学', '2026-07-15 08:00:00', '2026-07-15 16:00:00', 25, 0, 'ONGOING', 1),
(19, '英语演讲比赛', '命题演讲 + 即兴问答，评委打分', '教学楼B101', '2026-07-08 14:00:00', '2026-07-08 17:00:00', 50, 0, 'ONGOING', 1),
(20, '新生辩论赛', '辩题：人工智能是否应该取代人类决策', '模拟法庭', '2026-07-12 13:00:00', '2026-07-12 17:00:00', 40, 0, 'DRAFT', 1),
(21, '动漫周边展览', '社员原创画作及手办收藏展览', '图书馆一楼展厅', '2026-07-06 09:00:00', '2026-07-06 17:00:00', 100, 0, 'ONGOING', 1),
(12, '羽毛球挑战赛', '单打/双打淘汰赛，前三名有奖品', '体育馆羽毛球馆', '2026-07-04 14:00:00', '2026-07-04 18:00:00', 32, 0, 'ONGOING', 1);

-- ============================================
-- 常用操作 SQL（参考）
-- ============================================

-- 社团成员重新申请（QUIT/REJECTED → PENDING）
-- UPDATE club_member
-- SET status = 'PENDING',
--     role = 'MEMBER',
--     apply_reason = '新的申请理由',
--     joined_at = NULL,
--     quit_at = NULL,
--     created_at = NOW()
-- WHERE id = ?;
