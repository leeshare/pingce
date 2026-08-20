package com.shaanxi.zhiping.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shaanxi.zhiping.common.CacheConstants;
import com.shaanxi.zhiping.common.PageResult;
import com.shaanxi.zhiping.dto.CollegeCreateDTO;
import com.shaanxi.zhiping.dto.CollegeListVO;
import com.shaanxi.zhiping.dto.CollegeQueryDTO;
import com.shaanxi.zhiping.entity.College;
import com.shaanxi.zhiping.exception.BusinessException;
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

    // ==================== 管理后台 CRUD ====================

    /**
     * 新增院校
     */
    public Long create(CollegeCreateDTO dto) {
        College college = new College();
        applyDto(college, dto);
        collegeMapper.insert(college);
        return college.getId();
    }

    /**
     * 更新院校
     */
    public Boolean update(CollegeCreateDTO dto) {
        College college = loadById(dto.getId());
        applyDto(college, dto);
        collegeMapper.updateById(college);
        // 清除详情缓存
        redisUtil.delete(CacheConstants.COLLEGE_DETAIL_PREFIX + college.getId());
        return true;
    }

    /**
     * 删除院校（逻辑删除）
     */
    public Boolean delete(Long id) {
        collegeMapper.deleteById(id);
        redisUtil.delete(CacheConstants.COLLEGE_DETAIL_PREFIX + id);
        return true;
    }

    private College loadById(Long id) {
        College college = collegeMapper.selectById(id);
        if (college == null) {
            throw new BusinessException("院校不存在");
        }
        return college;
    }

    private void applyDto(College college, CollegeCreateDTO dto) {
        college.setName(dto.getName());
        college.setCode(dto.getCode());
        college.setNature(dto.getNature());
        college.setType(dto.getType());
        college.setLevel(dto.getLevel());
        college.setIsDoubleHigh(dto.getIsDoubleHigh() == null ? 0 : dto.getIsDoubleHigh());
        college.setProvince(dto.getProvince());
        college.setCity(dto.getCity());
        college.setLogo(dto.getLogo());
        college.setIntro(dto.getIntro());
    }
}
