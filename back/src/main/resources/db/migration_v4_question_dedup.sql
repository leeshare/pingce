-- ============================================================
-- 题库去重：基于 (content_hash, type) 唯一索引防止重复入库
-- 1. 新增 content_hash 列（题干归一化后的 SHA-1 短哈希，16位十六进制）
-- 2. 复合唯一索引 uk_content_hash_type_dep 纳入 deleted，
--    允许"逻辑删除的题"被重新录入同题干的新题
-- 3. 回填历史数据的 content_hash
-- ============================================================

ALTER TABLE `t_question`
  ADD COLUMN `content_hash` CHAR(16) NOT NULL DEFAULT '' COMMENT '题干归一化后的 SHA-1 短哈希(前16位)，用于查重'
  AFTER `content`;

-- 回填历史数据：content_hash = SHA1(LOWER(TRIM(content)) | type) 前 16 位
UPDATE `t_question`
SET `content_hash` = LEFT(SHA1(CONCAT(LOWER(TRIM(`content`)), '|', `type`)), 16)
WHERE `content_hash` = '';

-- 唯一索引：同一 (content_hash, type) 在未删除状态下仅允许一条
-- 说明：MySQL 唯一索引中 NULL 不参与去重，但 deleted=0/1 是确定值，故需搭配
ALTER TABLE `t_question`
  ADD UNIQUE KEY `uk_hash_type_dep` (`content_hash`, `type`, `deleted`);

-- 兜底索引：应用层按 (content_hash, type) + deleted=0 查重，加普通索引提升查询速度
ALTER TABLE `t_question`
  ADD KEY `idx_hash_type` (`content_hash`, `type`);
