package com.shaanxi.zhiping.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shaanxi.zhiping.dto.PaperQueryDTO;
import com.shaanxi.zhiping.entity.Paper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 试卷 Mapper
 */
@Mapper
public interface PaperMapper extends BaseMapper<Paper> {

    /**
     * 分页查询试卷列表
     */
    IPage<Paper> selectPaperPage(Page<Paper> page, @Param("q") PaperQueryDTO query);

    /**
     * 反查：question_ids 字段中包含给定任一题目 ID 的试卷列表（用于删除试题前的引用校验）。
     * 仅查未逻辑删除的试卷。
     */
    List<Paper> selectByQuestionIdIn(@Param("ids") List<Long> ids);
}
