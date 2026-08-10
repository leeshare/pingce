package com.shaanxi.zhiping.dto;

import lombok.Data;

/**
 * 首页统计数据
 */
@Data
public class HomeStatsVO {
    /** 今日刷题数 */
    private Integer today;
    /** 累计刷题数 */
    private Integer total;
    /** 正确率（百分比，0-100） */
    private Integer accuracy;
}
