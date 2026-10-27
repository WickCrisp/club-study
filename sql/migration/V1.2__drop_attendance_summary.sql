USE club_system;

-- ============================================
-- 阶段 A 迁移：下线签到签退与活动总结
-- 依据功能树：这两个能力不在范围内，代码与表一并移除
-- ============================================
DROP TABLE IF EXISTS `activity_checkin`;
DROP TABLE IF EXISTS `activity_summary`;

SELECT table_name AS remaining_tables
FROM information_schema.tables
WHERE table_schema = 'club_system'
ORDER BY table_name;
