package com.shaanxi.zhiping.dto;

import lombok.Data;

/**
 * 试卷查询 DTO
 */
@Data
public class PaperQueryDTO {
    /** 业务分区 1单招 2普通 3中考 4高考 5考研 */
    private Integer bizSection;
    /** 分类ID */
    private Long categoryId;
    /** 状态 0草稿 1已发布 */
    private Integer status;
    /** 真题年份 */
    private Integer year;
    /** 关键字（试卷名称） */
    private String keyword;
    /** 页码 */
    private Integer page = 1;
    /** 每页数量 */
    private Integer size = 10;
}
