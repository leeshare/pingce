package com.shaanxi.zhiping.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shaanxi.zhiping.entity.WrongQuestion;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 错题本 Mapper
 */
@Mapper
public interface WrongQuestionMapper extends BaseMapper<WrongQuestion> {

    /**
     * 上报错题（upsert）
     * 利用唯一索引 uk_user_question(user_id, question_id)：
     *  - 不存在：插入新记录，wrong_count=1
     *  - 已存在：wrong_count+1，mastered 重置为 0（再次错说明未掌握）
     *
     * 注意：MySQL INSERT ... ON DUPLICATE KEY UPDATE 在 upsert 时
     *       会触发 created_at 的 DEFAULT CURRENT_TIMESTAMP（新行），
     *       已存在行 updated_at 由 ON UPDATE CURRENT_TIMESTAMP 自动更新。
     */
    @Insert("INSERT INTO t_wrong_question(user_id, question_id, wrong_count, mastered, created_at, updated_at) " +
            "VALUES(#{userId}, #{questionId}, 1, 0, NOW(), NOW()) " +
            "ON DUPLICATE KEY UPDATE wrong_count = wrong_count + 1, mastered = 0, updated_at = NOW()")
    int upsert(@Param("userId") Long userId, @Param("questionId") Long questionId);
}
