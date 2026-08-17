-- 题库分类种子数据
-- 解决：t_question_category 表为空，导致导入页面无法选择分类，试题分类显示异常
-- 试题的 category_id=1 对应"语文"分类

USE `zhiping`;

-- 插入一级分类（按业务分区）
INSERT INTO `t_question_category` (`id`, `parent_id`, `name`, `sort`, `deleted`) VALUES
    (1, 0, '语文', 1, 0),
    (2, 0, '数学', 2, 0),
    (3, 0, '英语', 3, 0),
    (4, 0, '政治', 4, 0),
    (5, 0, '历史', 5, 0),
    (6, 0, '地理', 6, 0),
    (7, 0, '物理', 7, 0),
    (8, 0, '化学', 8, 0),
    (9, 0, '生物', 9, 0),
    (10, 0, '信息技术', 10, 0),
    (11, 0, '通用技术', 11, 0)
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`), `deleted` = 0;
