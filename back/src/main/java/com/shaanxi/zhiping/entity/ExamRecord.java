package com.shaanxi.zhiping.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 模考记录实体
 *
 * 对应表 t_exam_record，每次模拟考试交卷后写入一条记录。
 */
@Data
@TableName("t_exam_record")
public class ExamRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 试卷ID */
    private Long paperId;

    /** 得分 */
    private Integer score;

    /** 总分 */
    private Integer totalScore;

    /** 用时(秒) */
    private Integer duration;

    /** 答题详情JSON */
    private String answers;

    /** 交卷时间 */
    private LocalDateTime submitTime;

    /** 状态 0未交卷 1已交卷 */
    private Integer status;

    private LocalDateTime createdAt;
}
