package com.shaanxi.zhiping.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shaanxi.zhiping.dto.CollegeListVO;
import com.shaanxi.zhiping.dto.CollegeQueryDTO;
import com.shaanxi.zhiping.entity.College;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CollegeMapper extends BaseMapper<College> {

    /**
     * 分页查询院校列表
     */
    IPage<CollegeListVO> selectCollegePage(Page<CollegeListVO> page, @Param("q") CollegeQueryDTO query);
}
