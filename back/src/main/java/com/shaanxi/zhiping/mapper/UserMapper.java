package com.shaanxi.zhiping.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shaanxi.zhiping.dto.StudentQueryDTO;
import com.shaanxi.zhiping.dto.StudentVO;
import com.shaanxi.zhiping.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 统计用户今日刷题数
     */
    @Select("SELECT COUNT(*) FROM t_practice_record WHERE user_id = #{userId} AND created_at >= #{todayStart}")
    int countTodayPractice(@Param("userId") Long userId, @Param("todayStart") LocalDateTime todayStart);

    /**
     * 统计用户累计刷题数
     */
    @Select("SELECT COUNT(*) FROM t_practice_record WHERE user_id = #{userId}")
    int countTotalPractice(@Param("userId") Long userId);

    /**
     * 统计用户累计正确数
     */
    @Select("SELECT COUNT(*) FROM t_practice_record WHERE user_id = #{userId} AND is_correct = 1")
    int countCorrectPractice(@Param("userId") Long userId);

    /**
     * 后台学员分页查询：t_user LEFT JOIN t_practice_record 聚合统计
     * 一次 SQL 拿到 用户基础信息 + 累计刷题数 + 正确数 + 最近刷题时间，避免 N+1
     */
    IPage<StudentVO> selectStudentPage(Page<StudentVO> page, @Param("q") StudentQueryDTO query);
}
