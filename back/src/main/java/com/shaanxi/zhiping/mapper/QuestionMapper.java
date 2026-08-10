package com.shaanxi.zhiping.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shaanxi.zhiping.dto.QuestionQueryDTO;
import com.shaanxi.zhiping.entity.Question;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 题目 Mapper
 */
@Mapper
public interface QuestionMapper extends BaseMapper<Question> {

    /**
     * 分页查询题目列表
     */
    IPage<Question> selectQuestionPage(Page<Question> page, @Param("q") QuestionQueryDTO query);
}
