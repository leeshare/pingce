package com.shaanxi.zhiping.dto;

import lombok.Data;

/**
 * 院校列表项 VO
 */
@Data
public class CollegeListVO {
    private Long id;
    private String name;
    private String nature;
    private String type;
    private Integer isDoubleHigh;
    private String city;
    private String logo;
    /** 专业数量（冗余字段，方便前端展示，无则 0） */
    private Integer majorCount;
    /** 首字（用于头像占位） */
    private String initial;
}
