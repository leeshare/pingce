package com.shaanxi.zhiping.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shaanxi.zhiping.common.CacheConstants;
import com.shaanxi.zhiping.entity.QuestionCategory;
import com.shaanxi.zhiping.mapper.QuestionCategoryMapper;
import com.shaanxi.zhiping.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 题库分类 Service（集成 Redis 缓存）
 *
 * 缓存策略：
 * - 分类树（全量）：缓存 2 小时，分类变更时清除
 * - 分类详情：缓存 1 小时，更新/删除时清除单条
 */
@Slf4j
@Service
public class QuestionCategoryService {

    @Resource
    private QuestionCategoryMapper questionCategoryMapper;

    @Resource
    private RedisUtil redisUtil;

    /**
     * 查询全部分类（按排序，带缓存）
     * 小程序首页/分类选择器高频调用，命中率极高
     */
    public List<QuestionCategory> listAll() {
        List<QuestionCategory> cached = redisUtil.getList(CacheConstants.CATEGORY_TREE_KEY);
        if (cached != null) {
            log.debug("分类树命中缓存");
            return cached;
        }
        LambdaQueryWrapper<QuestionCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(QuestionCategory::getSort);
        List<QuestionCategory> list = questionCategoryMapper.selectList(wrapper);
        redisUtil.setList(CacheConstants.CATEGORY_TREE_KEY, list, CacheConstants.TTL_CATEGORY_TREE);
        return list;
    }

    /**
     * 按父分类查询子分类（带缓存）
     */
    public List<QuestionCategory> listByParent(Long parentId) {
        String cacheKey = "category:children:" + parentId;
        List<QuestionCategory> cached = redisUtil.getList(cacheKey);
        if (cached != null) {
            return cached;
        }
        LambdaQueryWrapper<QuestionCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QuestionCategory::getParentId, parentId)
               .orderByAsc(QuestionCategory::getSort);
        List<QuestionCategory> list = questionCategoryMapper.selectList(wrapper);
        redisUtil.setList(cacheKey, list, CacheConstants.TTL_CATEGORY_DETAIL);
        return list;
    }

    /**
     * 分类详情（带缓存）
     */
    public QuestionCategory detail(Long id) {
        String cacheKey = CacheConstants.CATEGORY_DETAIL_PREFIX + id;
        QuestionCategory cached = redisUtil.get(cacheKey);
        if (cached != null) {
            return cached;
        }
        QuestionCategory category = questionCategoryMapper.selectById(id);
        if (category != null) {
            redisUtil.set(cacheKey, category, CacheConstants.TTL_CATEGORY_DETAIL);
        }
        return category;
    }

    /**
     * 新增分类
     */
    public Long create(QuestionCategory category) {
        questionCategoryMapper.insert(category);
        clearCategoryCache();
        return category.getId();
    }

    /**
     * 更新分类
     */
    public boolean update(QuestionCategory category) {
        boolean ok = questionCategoryMapper.updateById(category) > 0;
        if (ok) {
            clearCategoryCache();
            redisUtil.delete(CacheConstants.CATEGORY_DETAIL_PREFIX + category.getId());
        }
        return ok;
    }

    /**
     * 删除分类
     */
    public boolean delete(Long id) {
        boolean ok = questionCategoryMapper.deleteById(id) > 0;
        if (ok) {
            clearCategoryCache();
            redisUtil.delete(CacheConstants.CATEGORY_DETAIL_PREFIX + id);
        }
        return ok;
    }

    /**
     * 清除分类相关缓存
     */
    private void clearCategoryCache() {
        redisUtil.delete(CacheConstants.CATEGORY_TREE_KEY);
        redisUtil.deleteByPrefix("category:children:");
    }
}
