package com.shaanxi.zhiping.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 错题本实体
 *
 * 对应表 t_wrong_question，唯一索引 uk_user_question(user_id, question_id)
 * 保证一个用户对一道题只有一条记录，重复错时累加 wrong_count。
 */
@Data
@TableName("t_wrong_question")
public class WrongQuestion {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 题目ID（复合题时为子题ID） */
    private Long questionId;

    /** 错误次数 */
    private Integer wrongCount;

    /** 是否已掌握 0否 1是 */
    private Integer mastered;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
