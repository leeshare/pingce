package com.shaanxi.zhiping.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 模考作答详情 VO
 *
 * 包含：试卷信息 + 题目列表（含正确答案/解析）+ 用户作答数据
 * 前端据此还原答题详情页，逐题展示用户答案、正确答案、对错、解析。
 */
@Data
public class ExamRecordDetailVO {

    /** 记录ID */
    private Long recordId;

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

    /** 题目列表（含正确答案/解析，复用 PracticeQuestionVO） */
    private List<PracticeQuestionVO> questions;

    /** 用户作答数据（与 /api/practice/grade 提交格式一致） */
    private List<PracticeGradeDTO.AnswerDTO> userAnswers;
}
