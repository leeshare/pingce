-- ============================================================
-- v9: 扩展 t_question / t_question_deleted 字段，支持新 Excel 导入
-- ============================================================
-- 背景：
--   新版 Excel（如 2014年甲卷_数学_单招真题.xlsx）在原 13 列基础上新增 6 列：
--     知识点编码、能力层级、辅助能力层级、核心素养、主题语境、难度系数P
--   同时旧版 Excel（如 英语_单招真题.xlsx）已有"课程结构"列但 DB 表未对应字段。
--   本次同步新增 7 个字段，主表与归档表保持一致。
--   旧 Excel 缺列时自动取 NULL，导入不报错。
-- ------------------------------------------------------------

-- 1) t_question 新增 7 个字段
ALTER TABLE `t_question`
  ADD COLUMN `course_structure`   varchar(128) DEFAULT NULL COMMENT '课程结构（旧Excel已存在）'        AFTER `score`,
  ADD COLUMN `knowledge_code`     varchar(64)  DEFAULT NULL COMMENT '知识点编码（如 SX.03.01.01.01）'   AFTER `options`,
  ADD COLUMN `ability_level`      varchar(16)  DEFAULT NULL COMMENT '能力层级（如 L3）'                 AFTER `knowledge_code`,
  ADD COLUMN `ability_level_aux`  varchar(16)  DEFAULT NULL COMMENT '辅助能力层级'                      AFTER `ability_level`,
  ADD COLUMN `core_literacy`      varchar(128) DEFAULT NULL COMMENT '核心素养（如 SX-S5;SX-S2）'        AFTER `ability_level_aux`,
  ADD COLUMN `theme_context`      varchar(128) DEFAULT NULL COMMENT '主题语境'                          AFTER `core_literacy`,
  ADD COLUMN `difficulty_p`        decimal(4,3) DEFAULT NULL COMMENT '难度系数P（0~1，如 0.8）'          AFTER `theme_context`;

-- 2) t_question_deleted 同步新增 7 个字段（与主表保持一致，便于归档留痕）
ALTER TABLE `t_question_deleted`
  ADD COLUMN `course_structure`   varchar(128) DEFAULT NULL COMMENT '课程结构'                          AFTER `score`,
  ADD COLUMN `knowledge_code`     varchar(64)  DEFAULT NULL COMMENT '知识点编码'                        AFTER `options`,
  ADD COLUMN `ability_level`      varchar(16)  DEFAULT NULL COMMENT '能力层级'                          AFTER `knowledge_code`,
  ADD COLUMN `ability_level_aux`  varchar(16)  DEFAULT NULL COMMENT '辅助能力层级'                      AFTER `ability_level`,
  ADD COLUMN `core_literacy`      varchar(128) DEFAULT NULL COMMENT '核心素养'                          AFTER `ability_level_aux`,
  ADD COLUMN `theme_context`      varchar(128) DEFAULT NULL COMMENT '主题语境'                          AFTER `core_literacy`,
  ADD COLUMN `difficulty_p`        decimal(4,3) DEFAULT NULL COMMENT '难度系数P'                          AFTER `theme_context`;
