package com.shaanxi.zhiping.dto;

import lombok.Data;

import java.util.List;

/**
 * 真题练习 / 模拟考试 批量判分请求 DTO
 *
 * 前端提交用户作答数据，后端统一判分并记录错题。
 * 复合题(type=7)通过 subAnswers 传递子题作答。
 */
@Data
public class PracticeGradeDTO {

    /** 试卷ID（模拟考试时传，真题练习可不传） */
    private Long paperId;

    /** 用时（秒），模拟考试交卷时由前端传入，用于记录到 t_exam_record */
    private Integer durationSec;

    /** 作答列表 */
    private List<AnswerDTO> answers;

    @Data
    public static class AnswerDTO {
        /** 题目ID */
        private Long questionId;

        /** 题型 1单选 2多选 3判断 4填空 5简答 6计算 7复合 */
        private Integer type;

        /** 选项索引（单选/多选/判断题使用，如 [0] 表示选了第1个选项） */
        private List<Integer> selected;

        /** 文本作答（填空/简答/计算题使用） */
        private String text;

        /** 复合题子题作答 */
        private List<AnswerDTO> subAnswers;
    }
}
