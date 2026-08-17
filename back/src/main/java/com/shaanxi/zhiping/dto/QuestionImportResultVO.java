package com.shaanxi.zhiping.dto;

import lombok.Data;

import java.util.List;

/**
 * 题目批量导入结果 VO
 */
@Data
public class QuestionImportResultVO {

    private String batchId;

    private Integer totalCount;

    private Integer successCount;

    private Integer failCount;

    /** 重复行数（DB 已存在或本批次内重复，已跳过；含在 failCount 内） */
    private Integer duplicateCount;

    /** 失败明细：[{row:2,msg:"题型非法"}] */
    private List<FailItem> failItems;

    @Data
    public static class FailItem {
        /** Excel 行号（从 2 开始，第 1 行为表头） */
        private Integer row;
        private String msg;

        public FailItem() {}

        public FailItem(Integer row, String msg) {
            this.row = row;
            this.msg = msg;
        }
    }
}
