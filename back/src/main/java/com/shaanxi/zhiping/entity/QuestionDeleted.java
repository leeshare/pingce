package com.shaanxi.zhiping.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 试题归档表实体：从 t_question 物理删除时归档到此表。
 * 归档表只作历史留痕，不校验重复——同一题被多次删除会留下多条记录。
 * 主键 id 由数据库自增，question_id 保存原 t_question ID。
 */
@Data
@TableName("t_question_deleted")
public class QuestionDeleted {

    /** 归档自增主键，由数据库 AUTO_INCREMENT 生成 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 原 t_question ID */
    private Long questionId;

    private Integer bizSection;

    private Long categoryId;

    private Long parentId;

    private Integer type;

    private String subType;

    private Integer sort;

    private Integer difficulty;

    private String content;

    private String contentHash;

    private String options;

    /** 知识点编码（如 SX.03.01.01.01） */
    private String knowledgeCode;

    /** 能力层级（如 L3） */
    private String abilityLevel;

    /** 辅助能力层级 */
    private String abilityLevelAux;

    /** 核心素养 */
    private String coreLiteracy;

    /** 主题语境 */
    private String themeContext;

    /** 难度系数P */
    private BigDecimal difficultyP;

    private String answer;

    private BigDecimal score;

    /** 课程结构 */
    private String courseStructure;

    private String analysis;

    private Integer year;

    private String source;

    private Integer status;

    private String importBatchId;

    private Long reviewerId;

    private String reviewRemark;

    private LocalDateTime reviewedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    /** 原逻辑删除标志，归档时强制为 1 */
    private Integer deleted;

    /** 归档时间 */
    private LocalDateTime deletedAt;
}
