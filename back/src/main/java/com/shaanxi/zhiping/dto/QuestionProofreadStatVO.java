package com.shaanxi.zhiping.dto;

import lombok.Data;

/**
 * 题库中心统计 VO（首页 / 校对 / 审核等仪表盘用）
 */
@Data
public class QuestionProofreadStatVO {

    /** 待校对题数（status=0 草稿态，与校对页列表默认查询条件一致） */
    private Long pendingProofread;

    /** 待审核题数（status=1） */
    private Long pendingReview;

    /** 已通过题数（status=2） */
    private Long approved;

    /** 已驳回题数（status=3） */
    private Long rejected;

    /** 草稿题数（status=0） */
    private Long draft;

    /** 题目总数 */
    private Long total;
}
