package com.shaanxi.zhiping.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 历史模考记录列表项 VO
 */
@Data
public class ExamRecordListVO {

    /** 记录ID */
    private Long id;

    /** 试卷ID */
    private Long paperId;

    /** 试卷标题 */
    private String paperTitle;

    /** 得分 */
    private Integer score;

    /** 总分 */
    private Integer totalScore;

    /** 用时(秒) */
    private Integer duration;

    /** 交卷时间 */
    private LocalDateTime submitTime;
}
