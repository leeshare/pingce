package com.shaanxi.zhiping.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shaanxi.zhiping.entity.QuestionImportBatch;
import org.apache.ibatis.annotations.Mapper;

/**
 * 题目批量导入批次 Mapper
 */
@Mapper
public interface QuestionImportBatchMapper extends BaseMapper<QuestionImportBatch> {
}
