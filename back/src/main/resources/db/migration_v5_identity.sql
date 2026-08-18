-- ============================================================
-- 身份模块数据库迁移脚本
-- 为 t_user 添加身份相关字段，并创建身份修改记录表
-- ============================================================

-- 1. 为 t_user 表添加身份相关字段
ALTER TABLE `t_user`
    ADD COLUMN `identity` VARCHAR(20) DEFAULT NULL COMMENT '考生身份 sanxiao-三校生 putong-普高' AFTER `gender`,
    ADD COLUMN `district` VARCHAR(32) DEFAULT NULL COMMENT '区县' AFTER `city`,
    ADD COLUMN `school` VARCHAR(128) DEFAULT NULL COMMENT '学校' AFTER `district`;

-- 2. 创建身份修改记录表
CREATE TABLE IF NOT EXISTS `t_identity_change_record` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`         BIGINT       NOT NULL COMMENT '用户ID',
    `original_identity` VARCHAR(20) DEFAULT NULL COMMENT '原身份类型',
    `original_province` VARCHAR(32) DEFAULT NULL COMMENT '原省份',
    `original_city`    VARCHAR(32)  DEFAULT NULL COMMENT '原城市',
    `original_district` VARCHAR(32) DEFAULT NULL COMMENT '原区县',
    `original_school`  VARCHAR(128) DEFAULT NULL COMMENT '原学校',
    `reason`          VARCHAR(512) NOT NULL COMMENT '修改原因',
    `status`          TINYINT      DEFAULT 0 COMMENT '状态 0待处理 1已通过 2已拒绝',
    `created_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`         TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_user` (`user_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='身份修改记录表';