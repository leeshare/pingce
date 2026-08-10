package com.shaanxi.zhiping.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shaanxi.zhiping.common.CacheConstants;
import com.shaanxi.zhiping.common.PageResult;
import com.shaanxi.zhiping.dto.QuestionQueryDTO;
import com.shaanxi.zhiping.entity.Question;
import com.shaanxi.zhiping.mapper.QuestionMapper;
import com.shaanxi.zhiping.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 题目 Service（集成 Redis 缓存）
 *
 * 缓存策略：
 * - 题目详情：缓存 30 分钟，更新/删除时清除单条缓存
 * - 复合题子题列表：缓存 30 分钟，父题更新/删除时清除
 * - 题目分页列表：缓存 10 分钟，任意增删改时按前缀批量清除
 */
@Slf4j
@Service
public class QuestionService {

    @Resource
    private QuestionMapper questionMapper;

    @Resource
    private RedisUtil redisUtil;

    /**
     * 分页查询题目（带缓存）
     * Key: question:list:{bizSection}:{categoryId}:{type}:{difficulty}:{parentId}:{page}:{size}
     */
    public PageResult<Question> listQuestions(QuestionQueryDTO query) {
        String cacheKey = buildListCacheKey(query);
        PageResult<Question> cached = redisUtil.get(cacheKey);
        if (cached != null) {
            log.debug("题目列表命中缓存 key={}", cacheKey);
            return cached;
        }

        int page = query.getPage() == null ? 1 : query.getPage();
        int size = query.getSize() == null ? 10 : query.getSize();
        Page<Question> p = new Page<>(page, size);
        IPage<Question> result = questionMapper.selectQuestionPage(p, query);
        PageResult<Question> pageResult = new PageResult<>(
                result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords());

        redisUtil.set(cacheKey, pageResult, CacheConstants.TTL_QUESTION_LIST);
        return pageResult;
    }

    /**
     * 题目详情（带缓存）
     */
    public Question detail(Long id) {
        String cacheKey = CacheConstants.QUESTION_DETAIL_PREFIX + id;
        Question cached = redisUtil.get(cacheKey);
        if (cached != null) {
            log.debug("题目详情命中缓存 id={}", id);
            return cached;
        }
        Question question = questionMapper.selectById(id);
        if (question != null) {
            redisUtil.set(cacheKey, question, CacheConstants.TTL_QUESTION_DETAIL);
        }
        return question;
    }

    /**
     * 查询复合题的子题列表（带缓存）
     */
    public List<Question> listChildren(Long parentId) {
        String cacheKey = CacheConstants.QUESTION_CHILDREN_PREFIX + parentId;
        List<Question> cached = redisUtil.getList(cacheKey);
        if (cached != null) {
            log.debug("子题列表命中缓存 parentId={}", parentId);
            return cached;
        }
        List<Question> list = questionMapper.selectList(
                new LambdaQueryWrapper<Question>()
                        .eq(Question::getParentId, parentId)
                        .orderByAsc(Question::getSort)
        );
        if (!list.isEmpty()) {
            redisUtil.setList(cacheKey, list, CacheConstants.TTL_QUESTION_CHILDREN);
        }
        return list;
    }

    /**
     * 新增题目
     */
    public Long create(Question question) {
        questionMapper.insert(question);
        // 清除列表缓存
        redisUtil.deleteByPrefix(CacheConstants.QUESTION_LIST_PREFIX);
        return question.getId();
    }

    /**
     * 更新题目
     */
    public boolean update(Question question) {
        boolean ok = questionMapper.updateById(question) > 0;
        if (ok) {
            // 清除单条缓存 + 列表缓存 + 子题缓存（若为复合题）
            Long id = question.getId();
            redisUtil.delete(CacheConstants.QUESTION_DETAIL_PREFIX + id);
            redisUtil.delete(CacheConstants.QUESTION_CHILDREN_PREFIX + id);
            redisUtil.deleteByPrefix(CacheConstants.QUESTION_LIST_PREFIX);
        }
        return ok;
    }

    /**
     * 删除题目
     */
    public boolean delete(Long id) {
        boolean ok = questionMapper.deleteById(id) > 0;
        if (ok) {
            redisUtil.delete(CacheConstants.QUESTION_DETAIL_PREFIX + id);
            redisUtil.delete(CacheConstants.QUESTION_CHILDREN_PREFIX + id);
            redisUtil.deleteByPrefix(CacheConstants.QUESTION_LIST_PREFIX);
        }
        return ok;
    }

    /**
     * 构建题目列表缓存 Key
     */
    private String buildListCacheKey(QuestionQueryDTO q) {
        return CacheConstants.QUESTION_LIST_PREFIX
                + nullToDefault(q.getBizSection(), 0) + ":"
                + nullToDefault(q.getCategoryId(), 0) + ":"
                + nullToDefault(q.getType(), 0) + ":"
                + nullToDefault(q.getDifficulty(), 0) + ":"
                + nullToDefault(q.getParentId(), 0) + ":"
                + nullToDefault(q.getPage(), 1) + ":"
                + nullToDefault(q.getSize(), 10);
    }

    private String nullToDefault(Object v, Object def) {
        return v == null ? String.valueOf(def) : String.valueOf(v);
    }
}
