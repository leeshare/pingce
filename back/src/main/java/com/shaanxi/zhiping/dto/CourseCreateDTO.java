package com.shaanxi.zhiping.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 课程新增/编辑 DTO
 */
@Data
public class CourseCreateDTO {

    private Long id;

    @NotBlank(message = "课程名称不能为空")
    private String title;

    /** 课程分类 语文/数学/英语/面试技巧 */
    private String category;

    private String cover;

    private String intro;

    /** 课程节数 */
    private Integer lessonCount;

    /** 课程价格 0为免费 */
    private BigDecimal price;

    private String location;

    private String teacher;

    private LocalDate startDate;

    /** 状态 0下架 1上架 */
    private Integer status;
}
