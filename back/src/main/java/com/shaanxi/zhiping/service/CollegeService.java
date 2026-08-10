package com.shaanxi.zhiping.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shaanxi.zhiping.common.CacheConstants;
import com.shaanxi.zhiping.common.PageResult;
import com.shaanxi.zhiping.dto.CollegeListVO;
import com.shaanxi.zhiping.dto.CollegeQueryDTO;
import com.shaanxi.zhiping.entity.College;
import com.shaanxi.zhiping.mapper.CollegeMapper;
import com.shaanxi.zhiping.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 院校 Service（集成 Redis 缓存）
 *
 * 缓存策略：
 * - 院校详情：缓存 1 小时，更新/删除时清除单条缓存
 */
@Slf4j
@Service
public class CollegeService {

    @Resource
    private CollegeMapper collegeMapper;

    @Resource
    private RedisUtil redisUtil;

    /**
     * 分页查询院校列表
     */
    public PageResult<CollegeListVO> listColleges(CollegeQueryDTO query) {
        int page = query.getPage() == null ? 1 : query.getPage();
        int size = query.getSize() == null ? 10 : query.getSize();
        Page<CollegeListVO> p = new Page<>(page, size);
        IPage<CollegeListVO> result = collegeMapper.selectCollegePage(p, query);
        return new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords());
    }

    /**
     * 获取院校详情（带缓存）
     * 院校信息变更频率低，缓存 1 小时
     */
    public College getCollegeDetail(Long id) {
        String cacheKey = CacheConstants.COLLEGE_DETAIL_PREFIX + id;
        College cached = redisUtil.get(cacheKey);
        if (cached != null) {
            log.debug("院校详情命中缓存 id={}", id);
            return cached;
        }
        College college = collegeMapper.selectById(id);
        if (college != null) {
            redisUtil.set(cacheKey, college, CacheConstants.TTL_COLLEGE_DETAIL);
        }
        return college;
    }
}
