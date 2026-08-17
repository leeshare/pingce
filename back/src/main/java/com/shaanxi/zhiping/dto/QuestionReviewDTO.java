package com.shaanxi.zhiping.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 试题审核 DTO
 */
@Data
public class QuestionReviewDTO {

    /** 题目ID列表（支持批量审核） */
    @NotEmpty(message = "题目ID不能为空")
    private List<Long> ids;

    /** 审核结果 2通过 3驳回 */
    @NotNull(message = "审核结果不能为空")
    private Integer status;

    /** 审核备注 */
    private String remark;
}
