package com.shaanxi.zhiping.dto;

import lombok.Data;

/**
 * 题目批量导入参数（除文件外的附加参数）
 */
@Data
public class QuestionImportDTO {

    /** 业务分区 1单招 2普通 3中考 4高考 5考研 */
    private Integer bizSection = 1;

    /** 默认分类ID（Excel 未填则使用） */
    private Long categoryId;

    /** 默认真题年份 */
    private Integer year;

    /** 默认来源 */
    private String source;

    /** 导入后状态：1待审核（默认） / 2直接通过 */
    private Integer status = 1;
}
