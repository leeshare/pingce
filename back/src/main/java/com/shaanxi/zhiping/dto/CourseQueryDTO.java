package com.shaanxi.zhiping.dto;

import lombok.Data;

/**
 * 课程查询 DTO
 */
@Data
public class CourseQueryDTO {

    /** 关键字：课程名称 */
    private String keyword;

    /** 课程分类 语文/数学/英语/面试技巧 */
    private String category;

    /** 状态 0下架 1上架 null 不限 */
    private Integer status;

    /** 是否免费 null 不限；true 仅免费；false 仅付费 */
    private Boolean free;

    /** 页码 */
    private Integer page = 1;

    /** 每页数量 */
    private Integer size = 10;
}
