package com.shaanxi.zhiping.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shaanxi.zhiping.entity.QuestionDeleted;
import org.apache.ibatis.annotations.Mapper;

/**
 * 试题归档表 Mapper（删除试题时归档插入用）
 */
@Mapper
public interface QuestionDeletedMapper extends BaseMapper<QuestionDeleted> {
}
