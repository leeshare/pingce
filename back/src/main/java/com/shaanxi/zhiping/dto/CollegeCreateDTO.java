package com.shaanxi.zhiping.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 院校新增/编辑 DTO
 */
@Data
public class CollegeCreateDTO {

    private Long id;

    /** 序号 */
    private Integer seq;

    @NotBlank(message = "院校名称不能为空")
    private String name;

    /** 院校代码 */
    private String code;

    /** 办学性质 公办/民办 */
    private String nature;

    /** 院校类型 综合/理工/师范等 */
    private String type;

    /** 层次 本科/专科 */
    private String level;

    /** 是否双高计划 0否 1是 */
    private Integer isDoubleHigh;

    private String province;

    private String city;

    /** 院校 logo URL */
    private String logo;

    /** 院校简介 */
    private String intro;

    /** 招生计划 */
    private Integer plan;

    /** 院校类别 国家示范/双高、省级示范等 */
    private String category;
}
