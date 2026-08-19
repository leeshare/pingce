package com.shaanxi.zhiping.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shaanxi.zhiping.dto.QuestionQueryDTO;
import com.shaanxi.zhiping.entity.Question;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 题目 Mapper
 */
@Mapper
public interface QuestionMapper extends BaseMapper<Question> {

    /**
     * 分页查询题目列表
     */
    IPage<Question> selectQuestionPage(Page<Question> page, @Param("q") QuestionQueryDTO query);

    /**
     * 物理删除：绕过 MyBatis-Plus 全局逻辑删除配置，真正执行 DELETE FROM。
     * 用于"归档到 t_question_deleted + 物理删除主表"模型。
     */
    int physicalDeleteByIds(@Param("ids") List<Long> ids);

    /**
     * 物理查询：绕过 MyBatis-Plus 全局逻辑删除配置的 selectById。
     * 用于删除流程中收集要归档的题目（即使 deleted=1 也要查到，以便后续清理）。
     */
    Question physicalSelectById(@Param("id") Long id);

    /**
     * 物理查询：绕过 MyBatis-Plus 全局逻辑删除配置，按 parent_id 查子题列表。
     */
    List<Question> physicalSelectByParentId(@Param("parentId") Long parentId);
}
