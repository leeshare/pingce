package com.shaanxi.zhiping.dto;

import lombok.Data;

/**
 * 题目查询 DTO
 */
@Data
public class QuestionQueryDTO {
    /** 业务分区 1单招 2普通 3中考 4高考 5考研 */
    private Integer bizSection;
    /** 分类ID */
    private Long categoryId;
    /** 题型 1单选 2多选 3判断 4填空 5简答 6计算 7复合 */
    private Integer type;
    /** 难度 1简单 2中等 3困难 */
    private Integer difficulty;
    /** 父题ID, 默认0表示独立题; -1表示查询复合题大题 */
    private Long parentId = 0L;
    /** 关键字（题干） */
    private String keyword;
    /** 真题年份 */
    private Integer year;
    /** 页码 */
    private Integer page = 1;
    /** 每页数量 */
    private Integer size = 10;
}
