-- ============================================================
-- 试题表改为物理删除模型
--
-- 背景：
--   t_question 原使用 deleted TINYINT 逻辑删除。重复导入场景下，旧记录 deleted=1
--   会与新插入的 deleted=1 同逻辑键冲突唯一索引；同时 MyBatis-Plus @TableLogic
--   会让 findDuplicate 只查 deleted=0，导致用户重新录入已删题时被错误放行
--   （DB 里已删的同键记录不在查询范围），形成"孤儿父行 / 重复子题"问题。
--
-- 方案：
--   新建归档表 t_question_deleted，结构与 t_question 一致（含原 deleted 列 +
--   deleted_at 归档时间）。删除时把记录搬到归档表后从主表物理 DELETE。
--   主表不再有 deleted=1 的脏数据，查重永远只看现存题，唯一索引可放心建为
--   (content_hash, type, biz_section, category_id, year) 5 列唯一。
-- ============================================================

-- 1) 创建归档表（结构与 t_question 完全一致，但去除唯一索引/自增主键约束）
CREATE TABLE IF NOT EXISTS `t_question_deleted` (
  `id` bigint NOT NULL COMMENT '原 t_question ID',
  `biz_section` tinyint DEFAULT NULL,
  `category_id` bigint DEFAULT NULL,
  `parent_id` bigint NOT NULL DEFAULT 0,
  `type` tinyint NOT NULL,
  `sub_type` varchar(64) DEFAULT NULL,
  `sort` int DEFAULT 0,
  `difficulty` tinyint DEFAULT NULL,
  `content` text NOT NULL,
  `content_hash` varchar(64) NOT NULL,
  `options` text,
  `answer` text,
  `score` decimal(6,2) DEFAULT NULL,
  `analysis` text,
  `year` int DEFAULT NULL,
  `source` varchar(128) DEFAULT NULL,
  `status` tinyint NOT NULL DEFAULT 1,
  `import_batch_id` varchar(64) DEFAULT NULL,
  `reviewer_id` bigint DEFAULT NULL,
  `review_remark` varchar(255) DEFAULT NULL,
  `reviewed_at` datetime DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 1 COMMENT '原逻辑删除标志，归档时强制为 1',
  `deleted_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '归档时间',
  PRIMARY KEY (`id`),
  KEY `idx_del_batch` (`import_batch_id`),
  KEY `idx_del_hash` (`content_hash`, `type`),
  KEY `idx_del_parent` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='试题归档表（已删除的题）';

-- 2) 把 t_question 中 deleted=1 的所有记录搬到归档表
INSERT INTO `t_question_deleted` (
  id, biz_section, category_id, parent_id, type, sub_type, sort, difficulty,
  content, content_hash, options, answer, score, analysis, year, source,
  status, import_batch_id, reviewer_id, review_remark, reviewed_at,
  created_at, updated_at, deleted, deleted_at
)
SELECT
  id, biz_section, category_id, parent_id, type, sub_type, sort, difficulty,
  content, content_hash, options, answer, score, analysis, year, source,
  status, import_batch_id, reviewer_id, review_remark, reviewed_at,
  created_at, updated_at, 1 AS deleted, NOW() AS deleted_at
FROM `t_question`
WHERE `deleted` = 1;

-- 3) 从主表物理删除已归档的记录
DELETE FROM `t_question` WHERE `deleted` = 1;

-- 4) 清理旧的去重索引（v5/v6 遗留）
SET @old_uk := (SELECT COUNT(*) FROM information_schema.STATISTICS
  WHERE table_schema = DATABASE() AND table_name = 't_question' AND index_name = 'uk_hash_type_dep');
SET @sql := IF(@old_uk > 0, 'ALTER TABLE `t_question` DROP INDEX `uk_hash_type_dep`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @old_idx := (SELECT COUNT(*) FROM information_schema.STATISTICS
  WHERE table_schema = DATABASE() AND table_name = 't_question' AND index_name = 'idx_hash_type');
SET @sql := IF(@old_idx > 0, 'ALTER TABLE `t_question` DROP INDEX `idx_hash_type`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @v6_idx := (SELECT COUNT(*) FROM information_schema.STATISTICS
  WHERE table_schema = DATABASE() AND table_name = 't_question' AND index_name = 'idx_hash_type_section_cat_year');
SET @sql := IF(@v6_idx > 0, 'ALTER TABLE `t_question` DROP INDEX `idx_hash_type_section_cat_year`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @v6_uk := (SELECT COUNT(*) FROM information_schema.STATISTICS
  WHERE table_schema = DATABASE() AND table_name = 't_question' AND index_name = 'uk_hash_type_section_cat_year_dep');
SET @sql := IF(@v6_uk > 0, 'ALTER TABLE `t_question` DROP INDEX `uk_hash_type_section_cat_year_dep`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 5) 重建 5 字段唯一索引（物理删除后无 deleted 列冲突）
--    content_hash 已对"题干+题型"归一化，加上 业务分区/分类/年份 后即 5 字段全等才视为重复
ALTER TABLE `t_question`
  ADD UNIQUE KEY `uk_hash_type_section_cat_year`
    (`content_hash`, `type`, `biz_section`, `category_id`, `year`);

-- 注：t_question.deleted 列保留但不再使用，避免立即改表结构影响线上服务。
--     后端代码不再依赖 deleted 列做查询过滤（去掉 @TableLogic）。
