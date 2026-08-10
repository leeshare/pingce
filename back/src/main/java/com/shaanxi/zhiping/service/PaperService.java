package com.shaanxi.zhiping.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shaanxi.zhiping.common.CacheConstants;
import com.shaanxi.zhiping.common.PageResult;
import com.shaanxi.zhiping.dto.PaperQueryDTO;
import com.shaanxi.zhiping.entity.Paper;
import com.shaanxi.zhiping.mapper.PaperMapper;
import com.shaanxi.zhiping.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 试卷 Service（集成 Redis 缓存）
 *
 * 缓存策略：
 * - 试卷详情：缓存 30 分钟，更新/删除时清除单条缓存
 */
@Slf4j
@Service
public class PaperService {

    @Resource
    private PaperMapper paperMapper;

    @Resource
    private RedisUtil redisUtil;

    /**
     * 分页查询试卷
     */
    public PageResult<Paper> listPapers(PaperQueryDTO query) {
        int page = query.getPage() == null ? 1 : query.getPage();
        int size = query.getSize() == null ? 10 : query.getSize();
        Page<Paper> p = new Page<>(page, size);
        IPage<Paper> result = paperMapper.selectPaperPage(p, query);
        return new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords());
    }

    /**
     * 试卷详情（带缓存）
     */
    public Paper detail(Long id) {
        String cacheKey = CacheConstants.PAPER_DETAIL_PREFIX + id;
        Paper cached = redisUtil.get(cacheKey);
        if (cached != null) {
            log.debug("试卷详情命中缓存 id={}", id);
            return cached;
        }
        Paper paper = paperMapper.selectById(id);
        if (paper != null) {
            redisUtil.set(cacheKey, paper, CacheConstants.TTL_PAPER_DETAIL);
        }
        return paper;
    }

    /**
     * 新增试卷
     */
    public Long create(Paper paper) {
        paperMapper.insert(paper);
        return paper.getId();
    }

    /**
     * 更新试卷
     */
    public boolean update(Paper paper) {
        boolean ok = paperMapper.updateById(paper) > 0;
        if (ok) {
            redisUtil.delete(CacheConstants.PAPER_DETAIL_PREFIX + paper.getId());
        }
        return ok;
    }

    /**
     * 删除试卷
     */
    public boolean delete(Long id) {
        boolean ok = paperMapper.deleteById(id) > 0;
        if (ok) {
            redisUtil.delete(CacheConstants.PAPER_DETAIL_PREFIX + id);
        }
        return ok;
    }
}
