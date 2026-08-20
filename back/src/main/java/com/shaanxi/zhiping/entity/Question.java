package com.shaanxi.zhiping.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
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

    /** 题干归一化后的 SHA-1 短哈希(前16位)，用于查重；DB 唯一索引 (content_hash,type,deleted) */
    private String contentHash;

    /** 选项JSON, 如["A.xxx","B.xxx"], 非选择题为NULL */
    private String options;

    /** 知识点编码（如 SX.03.01.01.01） */
    private String knowledgeCode;

    /** 能力层级（如 L3） */
    private String abilityLevel;

    /** 辅助能力层级 */
    private String abilityLevelAux;

    /** 核心素养（如 SX-S5;SX-S2，分号分隔） */
    private String coreLiteracy;

    /** 主题语境 */
    private String themeContext;

    /** 难度系数P（0~1，如 0.8） */
    private BigDecimal difficultyP;

    /** 正确答案: 单选"A", 多选"ABD", 填空["答1","答2"], 简答为参考范文 */
    private String answer;

    /** 分值 */
    private BigDecimal score;

    /** 课程结构（旧Excel已存在的列，多数为空） */
    private String courseStructure;

    /** 答案解析 */
    private String analysis;

    /** 真题年份 */
    private Integer year;

    private String source;

    /** 状态 0草稿 1待审核 2已通过 3已驳回 */
    private Integer status;

    /** 批量导入批次ID */
    private String importBatchId;

    /** 审核人ID */
    private Long reviewerId;

    /** 审核备注 */
    private String reviewRemark;

    /** 审核时间 */
    private LocalDateTime reviewedAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 逻辑删除标志（已废弃，保留只是为了向后兼容历史数据）。
     * 改为物理删除模型后，主表 t_question 不再有 deleted=1 的记录；
     * 删除时把行搬到归档表 t_question_deleted 后从主表 DELETE。
     * 此字段不再参与 MyBatis-Plus 自动过滤（已移除 @TableLogic）。
     */
    private Integer deleted;
}
