package com.shaanxi.zhiping.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 题目实体
 */
@Data
@TableName("t_question")
public class Question {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 业务分区 1单招 2普通 3中考 4高考 5考研 */
    private Integer bizSection;

    /** 分类ID */
    private Long categoryId;

    /** 父题ID, 0为独立题, 非0为复合题子题 */
    private Long parentId;

    /** 题型 1单选 2多选 3判断 4填空 5简答 6计算 7复合(大题) */
    private Integer type;

    /** 子题型, 如"阅读理解-推理判断" */
    private String subType;

    private Integer sort;

    /** 难度 1简单 2中等 3困难 */
    private Integer difficulty;

    /** 题干 */
    private String content;

    /** 选项JSON, 如["A.xxx","B.xxx"], 非选择题为NULL */
    private String options;

    /** 正确答案: 单选"A", 多选"ABD", 填空["答1","答2"], 简答为参考范文 */
    private String answer;

    /** 分值 */
    private BigDecimal score;

    /** 答案解析 */
    private String analysis;

    /** 真题年份 */
    private Integer year;

    private String source;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
