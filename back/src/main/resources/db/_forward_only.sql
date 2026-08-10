-- 正向变更脚本（从 migration_v1_question_upgrade.sql 提取）
USE `zhiping`;

-- 1. t_question 表结构升级
ALTER TABLE `t_question`
    MODIFY COLUMN `type` TINYINT NOT NULL
    COMMENT '题型 1单选 2多选 3判断 4填空 5简答 6计算 7复合(大题)';

ALTER TABLE `t_question`
    MODIFY COLUMN `options` TEXT DEFAULT NULL
    COMMENT '选项JSON, 如["A.xxx","B.xxx"], 非选择题为NULL';

ALTER TABLE `t_question`
    MODIFY COLUMN `answer` TEXT
    COMMENT '正确答案: 单选"A", 多选"ABD", 填空["答1","答2"], 简答为参考范文';

ALTER TABLE `t_question`
    ADD COLUMN `parent_id` BIGINT NOT NULL DEFAULT 0
    COMMENT '父题ID, 0为独立题, 非0为复合题子题'
    AFTER `category_id`;

ALTER TABLE `t_question`
    ADD COLUMN `score` DECIMAL(5,1) DEFAULT NULL
    COMMENT '分值'
    AFTER `answer`;

ALTER TABLE `t_question`
    ADD COLUMN `sub_type` VARCHAR(32) DEFAULT NULL
    COMMENT '子题型, 如"阅读理解-推理判断"'
    AFTER `type`;

ALTER TABLE `t_question`
    ADD COLUMN `biz_section` TINYINT NOT NULL DEFAULT 1
    COMMENT '业务分区 1单招 2普通 3中考 4高考 5考研'
    AFTER `id`;

ALTER TABLE `t_question`
    ADD COLUMN `sort` INT NOT NULL DEFAULT 0
    COMMENT '排序号'
    AFTER `sub_type`;

ALTER TABLE `t_question`
    ADD KEY `idx_parent` (`parent_id`),
    ADD KEY `idx_biz_section` (`biz_section`),
    ADD KEY `idx_section_category` (`biz_section`, `category_id`),
    ADD KEY `idx_category_diff` (`category_id`, `difficulty`),
    ADD KEY `idx_created` (`created_at`);

-- 2. t_paper 表新增业务分区
ALTER TABLE `t_paper`
    ADD COLUMN `biz_section` TINYINT NOT NULL DEFAULT 1
    COMMENT '业务分区 1单招 2普通 3中考 4高考 5考研'
    AFTER `id`;

ALTER TABLE `t_paper`
    ADD KEY `idx_biz_section` (`biz_section`);
