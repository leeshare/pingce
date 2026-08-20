package com.shaanxi.zhiping.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 课程实体
 */
@Data
@TableName("t_course")
public class Course {

    @TableId(type = IdType.AUTO)
    private Long id;

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

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
