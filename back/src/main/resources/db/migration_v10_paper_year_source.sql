-- ============================================================
-- v10: t_paper 新增 year / source 列，支持批量导入按真卷维度归集
-- ============================================================
-- 背景：
--   批量导入题目时，需按 (biz_section, category_id, year, source) 维度自动维护一套试卷。
--   原 t_paper 表缺少 year / source 列，无法精确区分"同一分类下不同年份/不同来源的真卷"。
--   本次补齐这两列，并加联合索引便于 upsert 查询。
-- 执行方式：mysql -u root -p zhiping < migration_v10_paper_year_source.sql
-- ============================================================

USE `zhiping`;

-- 1) 新增 year 列
ALTER TABLE `t_paper`
  ADD COLUMN `year` INT DEFAULT NULL COMMENT '真题年份' AFTER `category_id`;

-- 2) 新增 source 列
ALTER TABLE `t_paper`
  ADD COLUMN `source` VARCHAR(128) DEFAULT NULL COMMENT '来源（如 2025年乙(A)试卷）' AFTER `year`;

-- 3) 联合索引：用于按 (biz_section, category_id, year, source) upsert 查询
ALTER TABLE `t_paper`
  ADD KEY `idx_paper_key` (`biz_section`, `category_id`, `year`, `source`);
