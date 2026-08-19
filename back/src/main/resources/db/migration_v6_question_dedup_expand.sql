-- ============================================================
-- 题库去重规则升级：从 (content_hash, type) 扩展为
-- (content_hash, type, biz_section, category_id, year) 5 项全等才算重复
--
-- 说明：
--   content_hash 已对"题干+题型"做归一化指纹，本迁移把兜底索引扩展为
--   包含 业务分区 / 分类 / 年份 三列，与应用层 findDuplicate 保持一致。
--   历史数据无需重算 content_hash（指纹本身不变，只是去重维度增加）。
--
-- 注意：
--   本迁移不再创建 DB 唯一索引。原因：
--   t_question.deleted 是 TINYINT(0/1) 的逻辑删除标志，唯一索引纳入 deleted 后
--   会导致"同一逻辑键最多存在 1 条 deleted=1 记录"，再次删除同键题目会触发
--   Duplicate entry 异常（DELETE 实为 UPDATE deleted=0→1，受唯一索引约束）。
--   逻辑删除场景下，去重责任完全交给应用层 findDuplicate + Excel 导入循环
--   的 batchSeenKeys 即可，DB 仅保留普通索引加速查询。
-- ============================================================

-- 1) 删除旧的去重唯一索引（避免逻辑删除冲突）
SET @old_uk_exists := (SELECT COUNT(*) FROM information_schema.STATISTICS
  WHERE table_schema = DATABASE() AND table_name = 't_question' AND index_name = 'uk_hash_type_dep');
SET @sql := IF(@old_uk_exists > 0,
  'ALTER TABLE `t_question` DROP INDEX `uk_hash_type_dep`',
  'SELECT ''uk_hash_type_dep not exists, skipped'' AS msg');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 2) 删除旧的兜底索引（避免命名冲突）
SET @old_idx_exists := (SELECT COUNT(*) FROM information_schema.STATISTICS
  WHERE table_schema = DATABASE() AND table_name = 't_question' AND index_name = 'idx_hash_type');
SET @sql := IF(@old_idx_exists > 0,
  'ALTER TABLE `t_question` DROP INDEX `idx_hash_type`',
  'SELECT ''idx_hash_type not exists, skipped'' AS msg');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 3) 如上一版本（已执行的错误版本）误建了扩展唯一索引，也一并删除
SET @bad_uk_exists := (SELECT COUNT(*) FROM information_schema.STATISTICS
  WHERE table_schema = DATABASE() AND table_name = 't_question'
    AND index_name = 'uk_hash_type_section_cat_year_dep');
SET @sql := IF(@bad_uk_exists > 0,
  'ALTER TABLE `t_question` DROP INDEX `uk_hash_type_section_cat_year_dep`',
  'SELECT ''uk_hash_type_section_cat_year_dep not exists, skipped'' AS msg');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 4) 新建扩展后的兜底普通索引（应用层查重用，覆盖 5 列）
ALTER TABLE `t_question`
  ADD KEY `idx_hash_type_section_cat_year`
    (`content_hash`, `type`, `biz_section`, `category_id`, `year`);
