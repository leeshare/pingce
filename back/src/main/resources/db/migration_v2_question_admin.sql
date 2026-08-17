-- ============================================================
-- 陕西综评单招刷题小程序 - 题库中心管理后台升级脚本
-- 版本：v2.0
-- 日期：2026-08-17
-- 说明：
--   1. t_question 新增审核流程字段（status/reviewer_id/review_remark/reviewed_at）
--   2. t_question 新增批量导入批次字段（import_batch_id）
--   3. 新增 t_question_import_batch 批次表（追踪每次导入结果）
-- 执行方式：mysql -u root -p zhiping < migration_v2_question_admin.sql
-- ============================================================

USE `zhiping`;

-- ============================================================
-- 一、t_question 表新增字段
-- ============================================================

-- 1.1 新增审核状态：0草稿 1待审核 2已通过 3已驳回
ALTER TABLE `t_question`
    ADD COLUMN `status` TINYINT NOT NULL DEFAULT 1
    COMMENT '状态 0草稿 1待审核 2已通过 3已驳回'
    AFTER `source`;

-- 1.2 新增批量导入批次ID
ALTER TABLE `t_question`
    ADD COLUMN `import_batch_id` VARCHAR(64) DEFAULT NULL
    COMMENT '批量导入批次ID'
    AFTER `status`;

-- 1.3 新增审核人ID
ALTER TABLE `t_question`
    ADD COLUMN `reviewer_id` BIGINT DEFAULT NULL
    COMMENT '审核人ID'
    AFTER `import_batch_id`;

-- 1.4 新增审核备注
ALTER TABLE `t_question`
    ADD COLUMN `review_remark` VARCHAR(512) DEFAULT NULL
    COMMENT '审核备注'
    AFTER `reviewer_id`;

-- 1.5 新增审核时间
ALTER TABLE `t_question`
    ADD COLUMN `reviewed_at` DATETIME DEFAULT NULL
    COMMENT '审核时间'
    AFTER `review_remark`;

-- 1.6 新增索引
ALTER TABLE `t_question`
    ADD KEY `idx_status` (`status`),
    ADD KEY `idx_import_batch` (`import_batch_id`);

-- 1.7 历史数据默认置为"已通过"（题库中现有题目视为已审核）
UPDATE `t_question` SET `status` = 2, `reviewed_at` = NOW() WHERE `status` = 1 AND `deleted` = 0;

-- ============================================================
-- 二、新增 t_question_import_batch 批次表
-- ============================================================
CREATE TABLE IF NOT EXISTS `t_question_import_batch` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT,
    `batch_id`      VARCHAR(64)  NOT NULL COMMENT '批次ID（UUID）',
    `file_name`     VARCHAR(255) NOT NULL COMMENT '上传文件名',
    `file_size`     BIGINT       DEFAULT 0 COMMENT '文件大小(字节)',
    `total_count`   INT          DEFAULT 0 COMMENT 'Excel 解析出的总条数',
    `success_count` INT          DEFAULT 0 COMMENT '成功导入条数',
    `fail_count`    INT          DEFAULT 0 COMMENT '失败条数',
    `fail_detail`   TEXT         DEFAULT NULL COMMENT '失败明细JSON，如[{row:2,msg:"题型非法"}]',
    `status`        TINYINT      DEFAULT 0 COMMENT '状态 0处理中 1成功 2部分失败 3失败',
    `biz_section`   TINYINT      DEFAULT 1 COMMENT '业务分区',
    `category_id`   BIGINT       DEFAULT NULL COMMENT '默认分类ID',
    `year`          INT          DEFAULT NULL COMMENT '默认真题年份',
    `source`        VARCHAR(128) DEFAULT NULL COMMENT '默认来源',
    `created_by`    BIGINT       DEFAULT NULL COMMENT '操作人ID',
    `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_batch_id` (`batch_id`),
    KEY `idx_created` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目批量导入批次表';

-- ============================================================
-- 三、回滚脚本（谨慎执行）
-- ============================================================
-- ALTER TABLE `t_question`
--     DROP KEY `idx_import_batch`,
--     DROP KEY `idx_status`,
--     DROP COLUMN `reviewed_at`,
--     DROP COLUMN `review_remark`,
--     DROP COLUMN `reviewer_id`,
--     DROP COLUMN `import_batch_id`,
--     DROP COLUMN `status`;
--
-- DROP TABLE IF EXISTS `t_question_import_batch`;
