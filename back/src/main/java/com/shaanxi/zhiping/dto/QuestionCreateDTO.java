package com.shaanxi.zhiping.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 试题录入 / 编辑 DTO
 */
@Data
public class QuestionCreateDTO {

    /** 主键，更新时必填 */
    private Long id;

    /** 业务分区 1单招 2普通 3中考 4高考 5考研 */
    private Integer bizSection;

    private Long categoryId;

    /** 父题ID, 0为独立题, 非0为复合题子题 */
    private Long parentId;

    /** 题型 1单选 2多选 3判断 4填空 5简答 6计算 7复合 */
    private Integer type;

    private String subType;

    private Integer sort;

    /** 难度 1简单 2中等 3困难 */
    private Integer difficulty;

    private String content;

    /** 选项数组，如 ["A.xxx","B.xxx"] */
    private List<String> options;

    /** 答案：单选"A"，多选"ABD"，填空["答1","答2"]，简答为参考范文 */
    private String answer;

    private BigDecimal score;

    private String analysis;

    private Integer year;

    private String source;

    /** 状态 0草稿 1待审核 2已通过 3已驳回；不传则默认 1 */
    private Integer status;

    /** 提交类型：save_draft / submit_for_review；用于区分录入时是存草稿还是提交审核 */
    private String submitType;
}
