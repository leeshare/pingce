package com.shaanxi.zhiping.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 批量判分结果 VO
 *
 * 后端判分后返回每题结果 + 汇总信息。
 * 前端用此结果展示对错反馈、正确答案、解析。
 */
@Data
public class PracticeGradeResultVO {

    /** 每题判分结果 */
    private List<ItemResultVO> results;

    /** 汇总信息 */
    private SummaryVO summary;

    @Data
    public static class ItemResultVO {
        /** 题目ID */
        private Long questionId;

        /** 是否答对 */
        private Boolean isCorrect;

        /** 是否无参考答案（true=题库未录入正确答案，无法判分） */
        private Boolean noReference;

        /** 用户作答文本（用于展示） */
        private String userAnswerText;

        /** 正确答案文本（用于展示） */
        private String correctAnswerText;

        /** 复合题子题判分结果 */
        private List<ItemResultVO> subResults;
    }

    @Data
    public static class SummaryVO {
        /** 总题数 */
        private Integer total;

        /** 已作答题数 */
        private Integer answered;

        /** 答对数 */
        private Integer correct;

        /** 答错数 */
        private Integer wrong;

        /** 无参考答案数 */
        private Integer noRef;

        /** 得分 */
        private BigDecimal gotScore;
    }
}
