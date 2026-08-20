package com.shaanxi.zhiping.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 试卷实体
 */
@Data
@TableName("t_paper")
public class Paper {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 业务分区 1单招 2普通 3中考 4高考 5考研 */
    private Integer bizSection;

    private String title;

    private Long categoryId;

    /** 真题年份 */
    private Integer year;

    /** 来源（如 2025年乙(A)试卷） */
    private String source;

    private String description;

    /** 考试时长(分钟) */
    private Integer duration;

    private Integer totalScore;

    private Integer passScore;

    /** 题目ID列表, 逗号分隔 */
    private String questionIds;

    /** 状态 0草稿 1已发布 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
