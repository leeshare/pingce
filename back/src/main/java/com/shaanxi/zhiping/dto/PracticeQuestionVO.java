package com.shaanxi.zhiping.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 真题练习题目 VO（小程序端）
 *
 * 对应实体 {@link com.shaanxi.zhiping.entity.Question}，但做了三层裁剪：
 *  1. 去除管理字段（status / reviewerId / importBatchId / contentHash 等）
 *  2. options 已由 JSON 字符串解析为 {@link Option} 列表，前端无需再 parse
 *  3. 复合题(type=7) 的子题已嵌套到 subQuestions，前端无需二次请求
 *
 * answer 字段统一说明：
 *  - 单选(1): 字母，如 "A"
 *  - 多选(2): 字母串，如 "ABD"（前端按字符拆分）
 *  - 判断(3): "正确" / "错误"
 *  - 填空(4): 数组，如 ["答案1","答案2"]
 *  - 简答(5)/计算(6): 参考答案文本
 *  - 复合(7): 无 answer，参考子题 subQuestions
 */
@Data
public class PracticeQuestionVO {

    /** 题目ID */
    private Long id;

    /** 父题ID，0=独立题 */
    private Long parentId;

    /** 题型 1单选 2多选 3判断 4填空 5简答 6计算 7复合 */
    private Integer type;

    /** 题型名称，如 "单选题" */
    private String typeLabel;

    /** 子题型描述（可空），如"阅读理解-推理判断" */
    private String subType;

    /** 排序号 */
    private Integer sort;

    /** 难度 1简单 2中等 3困难 */
    private Integer difficulty;

    /** 难度名称 */
    private String difficultyLabel;

    /** 题干 */
    private String content;

    /** 选项列表，非选择题为 null */
    private List<Option> options;

    /** 正确答案（格式见类注释） */
    private Object answer;

    /** 分值 */
    private BigDecimal score;

    /** 解析 */
    private String analysis;

    /** 真题年份 */
    private Integer year;

    /** 来源 */
    private String source;

    /** 复合题子题列表，仅 type=7 时有值 */
    private List<PracticeQuestionVO> subQuestions;

    /**
     * 选项项
     */
    @Data
    public static class Option {
        /** 选项字母，如 "A" */
        private String letter;
        /** 选项文本 */
        private String text;
    }
}
