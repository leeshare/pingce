package com.shaanxi.zhiping.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
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
}
