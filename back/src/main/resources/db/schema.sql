-- ============================================================
-- 陕西综评单招刷题小程序 - 数据库建表脚本
-- 数据库：zhiping
-- 共 15 张核心表
-- ============================================================

CREATE DATABASE IF NOT EXISTS `zhiping` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `zhiping`;

-- 1. 用户表
CREATE TABLE IF NOT EXISTS `t_user` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `openid`      VARCHAR(64)  NOT NULL COMMENT '微信openid',
    `union_id`    VARCHAR(64)  DEFAULT NULL COMMENT '微信unionId',
    `nickname`    VARCHAR(64)  DEFAULT NULL COMMENT '昵称',
    `avatar`      VARCHAR(512) DEFAULT NULL COMMENT '头像URL',
    `phone`       VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    `gender`      TINYINT      DEFAULT 0 COMMENT '性别 0未知 1男 2女',
    `identity`    VARCHAR(20)  DEFAULT NULL COMMENT '考生身份 sanxiao-三校生 putong-普高',
    `province`    VARCHAR(32)  DEFAULT NULL COMMENT '省份',
    `city`        VARCHAR(32)  DEFAULT NULL COMMENT '城市',
    `district`    VARCHAR(32)  DEFAULT NULL COMMENT '区县',
    `school`      VARCHAR(128) DEFAULT NULL COMMENT '学校',
    `grade`       VARCHAR(20)  DEFAULT NULL COMMENT '年级',
    `target_major` VARCHAR(64) DEFAULT NULL COMMENT '意向专业',
    `member_level` TINYINT     DEFAULT 0 COMMENT '会员等级 0非会员 1月卡 2季卡 3年卡',
    `member_expire_time` DATETIME DEFAULT NULL COMMENT '会员过期时间',
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_openid` (`openid`),
    KEY `idx_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2. 题库分类表
CREATE TABLE IF NOT EXISTS `t_question_category` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `parent_id`   BIGINT       NOT NULL DEFAULT 0 COMMENT '父分类ID, 0为根',
    `name`        VARCHAR(64)  NOT NULL COMMENT '分类名称',
    `sort`        INT          DEFAULT 0 COMMENT '排序',
    `icon`        VARCHAR(512) DEFAULT NULL COMMENT '分类图标',
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`     TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_parent` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题库分类表';

-- 3. 题目表
CREATE TABLE IF NOT EXISTS `t_question` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT,
    `biz_section`   TINYINT      NOT NULL DEFAULT 1 COMMENT '业务分区 1单招 2普通 3中考 4高考 5考研',
    `category_id`   BIGINT       NOT NULL COMMENT '分类ID',
    `parent_id`     BIGINT       NOT NULL DEFAULT 0 COMMENT '父题ID, 0为独立题, 非0为复合题子题',
    `type`          TINYINT      NOT NULL COMMENT '题型 1单选 2多选 3判断 4填空 5简答 6计算 7复合(大题)',
    `sub_type`      VARCHAR(32)  DEFAULT NULL COMMENT '子题型, 如"阅读理解-推理判断"',
    `sort`          INT          NOT NULL DEFAULT 0 COMMENT '排序号',
    `difficulty`    TINYINT      DEFAULT 1 COMMENT '难度 1简单 2中等 3困难',
    `content`       TEXT         NOT NULL COMMENT '题干',
    `options`       TEXT         DEFAULT NULL COMMENT '选项JSON, 如["A.xxx","B.xxx"], 非选择题为NULL',
    `answer`        TEXT         COMMENT '正确答案: 单选"A", 多选"ABD", 填空["答1","答2"], 简答为参考范文',
    `score`         DECIMAL(5,1) DEFAULT NULL COMMENT '分值',
    `analysis`      TEXT         DEFAULT NULL COMMENT '答案解析',
    `year`          INT          DEFAULT NULL COMMENT '真题年份',
    `source`        VARCHAR(128) DEFAULT NULL COMMENT '来源',
    `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`       TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_category` (`category_id`),
    KEY `idx_year` (`year`),
    KEY `idx_parent` (`parent_id`),
    KEY `idx_biz_section` (`biz_section`),
    KEY `idx_section_category` (`biz_section`, `category_id`),
    KEY `idx_category_diff` (`category_id`, `difficulty`),
    KEY `idx_created` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目表';

-- 4. 试卷表
CREATE TABLE IF NOT EXISTS `t_paper` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT,
    `biz_section`  TINYINT      NOT NULL DEFAULT 1 COMMENT '业务分区 1单招 2普通 3中考 4高考 5考研',
    `title`        VARCHAR(128) NOT NULL COMMENT '试卷名称',
    `category_id`  BIGINT       NOT NULL COMMENT '所属分类',
    `description`  VARCHAR(512) DEFAULT NULL COMMENT '试卷说明',
    `duration`     INT          NOT NULL DEFAULT 90 COMMENT '考试时长(分钟)',
    `total_score`  INT          NOT NULL DEFAULT 100 COMMENT '总分',
    `pass_score`   INT          DEFAULT 60 COMMENT '及格分',
    `question_ids` TEXT         DEFAULT NULL COMMENT '题目ID列表, 逗号分隔',
    `status`       TINYINT      DEFAULT 0 COMMENT '状态 0草稿 1已发布',
    `created_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`      TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_category` (`category_id`),
    KEY `idx_biz_section` (`biz_section`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='试卷表';

-- 5. 刷题记录表
CREATE TABLE IF NOT EXISTS `t_practice_record` (
    `id`            BIGINT  NOT NULL AUTO_INCREMENT,
    `user_id`       BIGINT  NOT NULL,
    `question_id`   BIGINT  NOT NULL,
    `user_answer`   VARCHAR(20) DEFAULT NULL COMMENT '用户答案',
    `is_correct`    TINYINT DEFAULT NULL COMMENT '是否正确 0错 1对',
    `duration`      INT     DEFAULT 0 COMMENT '答题时长(秒)',
    `created_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user` (`user_id`),
    KEY `idx_question` (`question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='刷题记录表';

-- 6. 错题本表
CREATE TABLE IF NOT EXISTS `t_wrong_question` (
    `id`            BIGINT  NOT NULL AUTO_INCREMENT,
    `user_id`       BIGINT  NOT NULL,
    `question_id`   BIGINT  NOT NULL,
    `wrong_count`   INT     DEFAULT 1 COMMENT '错误次数',
    `mastered`      TINYINT DEFAULT 0 COMMENT '是否已掌握 0否 1是',
    `created_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_question` (`user_id`, `question_id`),
    KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='错题本表';

-- 7. 模考记录表
CREATE TABLE IF NOT EXISTS `t_exam_record` (
    `id`           BIGINT  NOT NULL AUTO_INCREMENT,
    `user_id`      BIGINT  NOT NULL,
    `paper_id`     BIGINT  NOT NULL,
    `score`        INT     DEFAULT NULL COMMENT '得分',
    `total_score`  INT     DEFAULT NULL COMMENT '总分',
    `duration`     INT     DEFAULT 0 COMMENT '用时(秒)',
    `answers`      TEXT    DEFAULT NULL COMMENT '答题详情JSON',
    `submit_time`  DATETIME DEFAULT NULL COMMENT '交卷时间',
    `status`       TINYINT DEFAULT 0 COMMENT '状态 0未交卷 1已交卷',
    `created_at`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user` (`user_id`),
    KEY `idx_paper` (`paper_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模考记录表';

-- 8. 院校表
CREATE TABLE IF NOT EXISTS `t_college` (
    `id`             BIGINT       NOT NULL AUTO_INCREMENT,
    `name`           VARCHAR(128) NOT NULL COMMENT '院校名称',
    `code`           VARCHAR(32)  DEFAULT NULL COMMENT '院校代码',
    `nature`         VARCHAR(16)  DEFAULT NULL COMMENT '办学性质 公办/民办',
    `type`           VARCHAR(32)  DEFAULT NULL COMMENT '院校类型 综合/理工/师范等',
    `level`          VARCHAR(32)  DEFAULT NULL COMMENT '层次 本科/专科',
    `is_double_high` TINYINT      DEFAULT 0 COMMENT '是否双高计划 0否 1是',
    `province`       VARCHAR(32)  DEFAULT NULL COMMENT '省份',
    `city`           VARCHAR(32)  DEFAULT NULL COMMENT '城市',
    `logo`           VARCHAR(512) DEFAULT NULL COMMENT '院校logo',
    `intro`          TEXT         DEFAULT NULL COMMENT '院校简介',
    `created_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`        TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_province` (`province`),
    KEY `idx_city` (`city`),
    KEY `idx_nature` (`nature`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='院校表';

-- 9. 院校分数线表
CREATE TABLE IF NOT EXISTS `t_score_line` (
    `id`           BIGINT  NOT NULL AUTO_INCREMENT,
    `college_id`   BIGINT  NOT NULL,
    `year`         INT     NOT NULL COMMENT '年份',
    `major`        VARCHAR(128) DEFAULT NULL COMMENT '专业',
    `score`        INT     DEFAULT NULL COMMENT '分数线',
    `rank`         INT     DEFAULT NULL COMMENT '位次',
    `type`         VARCHAR(32) DEFAULT NULL COMMENT '科类 文/理',
    `created_at`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_college` (`college_id`),
    KEY `idx_year` (`year`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='院校分数线表';

-- 10. 政策表
CREATE TABLE IF NOT EXISTS `t_policy` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `title`       VARCHAR(256) NOT NULL COMMENT '标题',
    `summary`     VARCHAR(512) DEFAULT NULL COMMENT '摘要',
    `content`     LONGTEXT     NOT NULL COMMENT '正文',
    `category`    VARCHAR(32)  DEFAULT NULL COMMENT '分类',
    `publish_date` DATE        DEFAULT NULL COMMENT '发布日期',
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`     TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='招生政策表';

-- 11. 会员套餐表
CREATE TABLE IF NOT EXISTS `t_member_package` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `name`        VARCHAR(64)  NOT NULL COMMENT '套餐名称',
    `type`        TINYINT      NOT NULL COMMENT '类型 1月卡 2季卡 3年卡',
    `price`       DECIMAL(10,2) NOT NULL COMMENT '价格(元)',
    `original_price` DECIMAL(10,2) DEFAULT NULL COMMENT '原价',
    `duration`    INT          NOT NULL COMMENT '时长(天)',
    `description` VARCHAR(512) DEFAULT NULL COMMENT '权益说明',
    `status`      TINYINT      DEFAULT 1 COMMENT '状态 0下架 1上架',
    `sort`        INT          DEFAULT 0,
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`     TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员套餐表';

-- 12. 订单表
CREATE TABLE IF NOT EXISTS `t_order` (
    `id`             BIGINT       NOT NULL AUTO_INCREMENT,
    `order_no`       VARCHAR(64)  NOT NULL COMMENT '订单号',
    `user_id`        BIGINT       NOT NULL,
    `package_id`     BIGINT       NOT NULL,
    `amount`          DECIMAL(10,2) NOT NULL COMMENT '实付金额',
    `pay_method`     VARCHAR(32)  DEFAULT 'wechat' COMMENT '支付方式',
    `wx_transaction_id` VARCHAR(64) DEFAULT NULL COMMENT '微信支付交易号',
    `status`         TINYINT      DEFAULT 0 COMMENT '状态 0待支付 1已支付 2已取消 3已退款',
    `paid_time`      DATETIME     DEFAULT NULL COMMENT '支付时间',
    `created_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 13. 线索表
CREATE TABLE IF NOT EXISTS `t_lead` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`     BIGINT       DEFAULT NULL,
    `name`        VARCHAR(64)  DEFAULT NULL COMMENT '姓名',
    `phone`       VARCHAR(20)  NOT NULL COMMENT '手机号',
    `source`      VARCHAR(64)  DEFAULT NULL COMMENT '来源 志愿测评/试听预约等',
    `intention`  VARCHAR(128) DEFAULT NULL COMMENT '意向描述',
    `status`      TINYINT      DEFAULT 0 COMMENT '状态 0新线索 1已联系 2已转化 3无效',
    `remark`      VARCHAR(512) DEFAULT NULL COMMENT '备注',
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_phone` (`phone`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户线索表';

-- 14. 课程表
CREATE TABLE IF NOT EXISTS `t_course` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `title`       VARCHAR(128) NOT NULL COMMENT '课程名称',
    `cover`       VARCHAR(512) DEFAULT NULL COMMENT '封面图',
    `intro`       TEXT         DEFAULT NULL COMMENT '课程介绍',
    `price`       DECIMAL(10,2) DEFAULT NULL COMMENT '课程价格',
    `location`    VARCHAR(128) DEFAULT NULL COMMENT '上课地点',
    `teacher`     VARCHAR(64)  DEFAULT NULL COMMENT '授课老师',
    `start_date`  DATE         DEFAULT NULL COMMENT '开课日期',
    `status`      TINYINT      DEFAULT 1 COMMENT '状态 0下架 1上架',
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`     TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='线下课程表';

-- 15. 收藏表
CREATE TABLE IF NOT EXISTS `t_favorite` (
    `id`          BIGINT  NOT NULL AUTO_INCREMENT,
    `user_id`     BIGINT  NOT NULL,
    `target_type` TINYINT NOT NULL COMMENT '收藏类型 1题目 2院校 3课程',
    `target_id`   BIGINT  NOT NULL COMMENT '目标ID',
    `created_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_target` (`user_id`, `target_type`, `target_id`),
    KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';

-- 16. 身份修改记录表
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
