package com.shaanxi.zhiping.service;

import com.shaanxi.zhiping.entity.Question;
import com.shaanxi.zhiping.mapper.QuestionMapper;
import com.shaanxi.zhiping.mapper.WrongQuestionMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 错题本 Service（小程序端）
 *
 * 仅负责"上报错题"这一个动作：upsert 到 t_wrong_question。
 * 错题列表查询、错题掌握标记等接口后续按需扩展。
 *
 * 上报时机由前端判定（用户作答错误且题库有正确答案），后端只做幂等入库。
 */
@Slf4j
@Service
public class WrongQuestionService {

    @Resource
    private WrongQuestionMapper wrongQuestionMapper;

    @Resource
    private QuestionMapper questionMapper;

    /**
     * 上报一道错题
     *
     * @param userId     用户ID
     * @param questionId 题目ID（复合题为子题ID）
     * @return true=入库成功；false=题目不存在或已删除
     */
    public boolean report(Long userId, Long questionId) {
        if (userId == null || questionId == null) {
            return false;
        }
        // 校验题目存在且未删除（防止脏数据）
        Question q = questionMapper.selectById(questionId);
        if (q == null) {
            log.warn("上报错题失败，题目不存在 questionId={}", questionId);
            return false;
        }
        wrongQuestionMapper.upsert(userId, questionId);
        return true;
    }
}
