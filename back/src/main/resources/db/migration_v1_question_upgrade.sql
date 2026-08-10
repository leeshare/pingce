-- ============================================================
-- 陕西综评单招刷题小程序 - 题库表结构升级脚本
-- 版本：v1.0
-- 日期：2026-08-08
-- 说明：
--   1. 扩展 t_question 题型支持（填空/简答/计算/复合题）
--   2. 新增业务分区字段 biz_section（单招/普通/中考/高考/考研）
--   3. t_paper 同步新增 biz_section
--   4. 补充高频查询复合索引
-- 执行方式：mysql -u root -p zhiping < migration_v1_question_upgrade.sql
-- 回滚方式：执行本文件末尾 "回滚脚本" 部分
-- ============================================================

USE `zhiping`;

-- ============================================================
-- 一、正向变更脚本
-- ============================================================

-- ---------- 1. t_question 表结构升级 ----------

-- 1.1 扩展 type 枚举注释：1单选 2多选 3判断 4填空 5简答 6计算 7复合
ALTER TABLE `t_question`
    MODIFY COLUMN `type` TINYINT NOT NULL
    COMMENT '题型 1单选 2多选 3判断 4填空 5简答 6计算 7复合(大题)';

-- 1.2 options 允许为空（非选择题无选项）
ALTER TABLE `t_question`
    MODIFY COLUMN `options` TEXT DEFAULT NULL
    COMMENT '选项JSON, 如["A.xxx","B.xxx"], 非选择题为NULL';

-- 1.3 answer 改为 TEXT 容纳多空答案/参考范文
--     单选"A", 多选"ABD", 填空["答案1","答案2"], 简答为参考范文
ALTER TABLE `t_question`
    MODIFY COLUMN `answer` TEXT
    COMMENT '正确答案: 单选"A", 多选"ABD", 填空["答1","答2"], 简答为参考范文';

-- 1.4 新增复合题父子关系
ALTER TABLE `t_question`
    ADD COLUMN `parent_id` BIGINT NOT NULL DEFAULT 0
    COMMENT '父题ID, 0为独立题, 非0为复合题子题'
    AFTER `category_id`;

-- 1.5 新增分值字段
ALTER TABLE `t_question`
    ADD COLUMN `score` DECIMAL(5,1) DEFAULT NULL
    COMMENT '分值'
    AFTER `answer`;

-- 1.6 新增子题型字段
ALTER TABLE `t_question`
    ADD COLUMN `sub_type` VARCHAR(32) DEFAULT NULL
    COMMENT '子题型, 如"阅读理解-推理判断"'
    AFTER `type`;

-- 1.7 新增业务分区字段
ALTER TABLE `t_question`
    ADD COLUMN `biz_section` TINYINT NOT NULL DEFAULT 1
    COMMENT '业务分区 1单招 2普通 3中考 4高考 5考研'
    AFTER `id`;

-- 1.8 新增排序字段（复合题子题排序）
ALTER TABLE `t_question`
    ADD COLUMN `sort` INT NOT NULL DEFAULT 0
    COMMENT '排序号'
    AFTER `sub_type`;

-- 1.9 新增索引
ALTER TABLE `t_question`
    ADD KEY `idx_parent` (`parent_id`),
    ADD KEY `idx_biz_section` (`biz_section`),
    ADD KEY `idx_section_category` (`biz_section`, `category_id`),
    ADD KEY `idx_category_diff` (`category_id`, `difficulty`),
    ADD KEY `idx_user_created` (`created_at`);

-- ---------- 2. t_paper 表新增业务分区 ----------

ALTER TABLE `t_paper`
    ADD COLUMN `biz_section` TINYINT NOT NULL DEFAULT 1
    COMMENT '业务分区 1单招 2普通 3中考 4高考 5考研'
    AFTER `id`;

ALTER TABLE `t_paper`
    ADD KEY `idx_biz_section` (`biz_section`);

-- ---------- 3. 数据迁移（可选）----------
-- 已有题目默认归入"单招"分区（biz_section=1），无需额外更新
-- 如需将历史数据标记为其他分区，可按需执行：
-- UPDATE `t_question` SET biz_section = 2 WHERE source LIKE '%普通%';
-- UPDATE `t_paper`   SET biz_section = 2 WHERE title LIKE '%普通%';

-- ============================================================
-- 二、回滚脚本
-- 注意：回滚会删除新增字段和索引，新增数据中的 biz_section /
--       parent_id / score / sub_type / sort 信息将丢失，请先备份。
-- ============================================================

-- ---------- 回滚 t_question ----------

-- 2.1 删除新增索引
ALTER TABLE `t_question`
    DROP KEY `idx_user_created`,
    DROP KEY `idx_category_diff`,
    DROP KEY `idx_section_category`,
    DROP KEY `idx_biz_section`,
    DROP KEY `idx_parent`;

-- 2.2 删除新增字段
ALTER TABLE `t_question`
    DROP COLUMN `sort`,
    DROP COLUMN `biz_section`,
    DROP COLUMN `sub_type`,
    DROP COLUMN `score`,
    DROP COLUMN `parent_id`;

-- 2.3 还原字段类型与注释
ALTER TABLE `t_question`
    MODIFY COLUMN `answer` VARCHAR(20) NOT NULL
    COMMENT '正确答案, 如 "A" 或 "ABD"';

ALTER TABLE `t_question`
    MODIFY COLUMN `options` TEXT NOT NULL
    COMMENT '选项JSON, 如["A.xxx","B.xxx"]';

ALTER TABLE `t_question`
    MODIFY COLUMN `type` TINYINT NOT NULL
    COMMENT '题型 1单选 2多选 3判断';

-- ---------- 回滚 t_paper ----------

ALTER TABLE `t_paper`
    DROP KEY `idx_biz_section`;

ALTER TABLE `t_paper`
    DROP COLUMN `biz_section`;
