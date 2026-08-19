-- ============================================================
-- v8: 调整 t_question_deleted 主键策略
-- ============================================================
-- 背景：
--   v7 把归档表主键设为 id（原 t_question ID），并在 Java 中用
--   try-insert / catch-update 兜底以避免主键冲突。
--   但用户要求：归档表只做历史留痕，不做重复校验，每次删除直接插入。
--   因此把主键改为自增 id，原 t_question ID 改名为 question_id 普通列。
--   这样同一道题被反复删除-重导时，归档表会保留多条历史记录。
-- ------------------------------------------------------------

-- 1) 把原 id 列改名为 question_id，并去掉 PRIMARY KEY 约束
ALTER TABLE `t_question_deleted`
  DROP PRIMARY KEY,
  CHANGE COLUMN `id` `question_id` bigint NOT NULL COMMENT '原 t_question ID';

-- 2) 新增自增主键列 id（放在首列）
ALTER TABLE `t_question_deleted`
  ADD COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT '归档自增主键' FIRST,
  ADD PRIMARY KEY (`id`);

-- 3) 在 question_id 上加普通索引（原主键索引已随主键去除）
ALTER TABLE `t_question_deleted`
  ADD INDEX `idx_del_qid` (`question_id`);
