package com.shaanxi.zhiping.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 院校实体
 */
@Data
@TableName("t_college")
public class College {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 序号 */
    private Integer seq;

    private String name;

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

    private String logo;

    private String intro;

    /** 招生计划 */
    private Integer plan;

    /** 院校类别 国家示范/双高、省级示范等 */
    private String category;

    @TableField(fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
