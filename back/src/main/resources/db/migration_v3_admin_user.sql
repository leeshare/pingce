-- ============================================================
-- 题库管理后台 - 管理员用户表
-- 1. 创建 t_admin_user 表
-- 2. 超管账号通过应用启动时 AdminBootstrap 初始化（admin / admin123）
-- ============================================================

CREATE TABLE IF NOT EXISTS `t_admin_user` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT,
  `username`      VARCHAR(64)  NOT NULL                    COMMENT '用户名',
  `password`      VARCHAR(128) NOT NULL                    COMMENT 'BCrypt 加密密码',
  `nickname`      VARCHAR(64)  DEFAULT NULL                COMMENT '昵称',
  `avatar`        VARCHAR(512) DEFAULT NULL                COMMENT '头像',
  `is_super`      TINYINT      NOT NULL DEFAULT 0          COMMENT '是否超管 0否 1是',
  `permissions`   VARCHAR(1024) DEFAULT ''                 COMMENT '权限码，逗号分隔；超管为 *',
  `status`        TINYINT      NOT NULL DEFAULT 1          COMMENT '0禁用 1启用',
  `last_login_at` DATETIME     DEFAULT NULL                COMMENT '最后登录时间',
  `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`       TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理后台用户';
