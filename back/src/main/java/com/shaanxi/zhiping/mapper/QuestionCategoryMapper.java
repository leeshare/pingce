package com.shaanxi.zhiping.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shaanxi.zhiping.entity.QuestionCategory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 题库分类 Mapper
 */
@Mapper
public interface QuestionCategoryMapper extends BaseMapper<QuestionCategory> {
}
