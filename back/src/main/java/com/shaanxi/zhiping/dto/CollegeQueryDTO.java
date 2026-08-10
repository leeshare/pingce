package com.shaanxi.zhiping.dto;

import lombok.Data;

/**
 * 院校查询 DTO
 */
@Data
public class CollegeQueryDTO {
    /** 关键字（院校名称） */
    private String keyword;
    /** 城市 */
    private String city;
    /** 办学性质 公办/民办 */
    private String nature;
    /** 类型 综合/理工等 */
    private String type;
    /** 是否双高计划 1是 0否 null 不限 */
    private Integer isDoubleHigh;
    /** 推荐排序 1是 0否 */
    private Integer recommend = 0;
    /** 页码 */
    private Integer page = 1;
    /** 每页数量 */
    private Integer size = 10;
}
