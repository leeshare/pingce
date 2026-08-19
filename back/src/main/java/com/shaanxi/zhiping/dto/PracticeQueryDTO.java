package com.shaanxi.zhiping.dto;

import lombok.Data;

/**
 * 真题练习查询 DTO（小程序端）
 *
 * 仅暴露年份 + 科目两个维度，业务分区固定为单招(bizSection=1)，
 * 状态固定为已通过(status=2)，由服务层强制写入，避免前端越权拉取。
 */
@Data
public class PracticeQueryDTO {
    /** 真题年份，如 2026 */
    private Integer year;

    /** 分类ID：1语文 2数学 3英语 ... */
    private Long categoryId;
}
