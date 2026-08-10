package com.shaanxi.zhiping.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shaanxi.zhiping.dto.PaperQueryDTO;
import com.shaanxi.zhiping.entity.Paper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 试卷 Mapper
 */
@Mapper
public interface PaperMapper extends BaseMapper<Paper> {

    /**
     * 分页查询试卷列表
     */
    IPage<Paper> selectPaperPage(Page<Paper> page, @Param("q") PaperQueryDTO query);
}
