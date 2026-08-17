package com.shaanxi.zhiping.dto;

import lombok.Data;

/**
 * 题目批量导入批次查询 DTO
 */
@Data
public class QuestionImportBatchQueryDTO {

    /** 关键字（文件名/批次ID） */
    private String keyword;

    /** 状态 0处理中 1成功 2部分失败 3失败 */
    private Integer status;

    private Integer page = 1;

    private Integer size = 10;
}
