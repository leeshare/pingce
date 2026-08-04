package com.shaanxi.zhiping.common;

import lombok.Data;

import java.util.List;

/**
 * 分页返回结果
 */
@Data
public class PageResult<T> {

    private Long total;
    private Long page;
    private Long size;
    private List<T> records;

    public PageResult() {}

    public PageResult(Long total, Long page, Long size, List<T> records) {
        this.total = total;
        this.page = page;
        this.size = size;
        this.records = records;
    }
}
