package com.shaanxi.zhiping.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shaanxi.zhiping.common.PageResult;
import com.shaanxi.zhiping.dto.CourseCreateDTO;
import com.shaanxi.zhiping.dto.CourseQueryDTO;
import com.shaanxi.zhiping.entity.Course;
import com.shaanxi.zhiping.exception.BusinessException;
import com.shaanxi.zhiping.mapper.CourseMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;

/**
 * 管理后台 - 课程管理 Service
 */
@Slf4j
@Service
public class CourseService {

    @Resource
    private CourseMapper courseMapper;

    /**
     * 分页查询
     */
    public PageResult<Course> page(CourseQueryDTO query) {
        int page = query.getPage() == null ? 1 : query.getPage();
        int size = query.getSize() == null ? 10 : query.getSize();
        Page<Course> p = new Page<>(page, size);
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(query.getKeyword())) {
            wrapper.like(Course::getTitle, query.getKeyword());
        }
        if (StrUtil.isNotBlank(query.getCategory())) {
            wrapper.eq(Course::getCategory, query.getCategory());
        }
        if (query.getStatus() != null) {
            wrapper.eq(Course::getStatus, query.getStatus());
        }
        if (Boolean.TRUE.equals(query.getFree())) {
            wrapper.and(w -> w.isNull(Course::getPrice).or().eq(Course::getPrice, BigDecimal.ZERO));
        } else if (Boolean.FALSE.equals(query.getFree())) {
            wrapper.gt(Course::getPrice, BigDecimal.ZERO);
        }
        wrapper.orderByDesc(Course::getId);
        IPage<Course> result = courseMapper.selectPage(p, wrapper);
        return new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords());
    }

    /**
     * 详情
     */
    public Course detail(Long id) {
        return loadById(id);
    }

    /**
     * 新增
     */
    public Long create(CourseCreateDTO dto) {
        Course course = new Course();
        applyDto(course, dto);
        courseMapper.insert(course);
        return course.getId();
    }

    /**
     * 更新
     */
    public Boolean update(CourseCreateDTO dto) {
        Course course = loadById(dto.getId());
        applyDto(course, dto);
        courseMapper.updateById(course);
        return true;
    }

    /**
     * 删除（逻辑删除）
     */
    public Boolean delete(Long id) {
        courseMapper.deleteById(id);
        return true;
    }

    /**
     * 上架/下架
     */
    public Boolean toggleStatus(Long id, Integer status) {
        Course course = loadById(id);
        course.setStatus(status);
        courseMapper.updateById(course);
        return true;
    }

    private Course loadById(Long id) {
        Course course = courseMapper.selectById(id);
        if (course == null) {
            throw new BusinessException("课程不存在");
        }
        return course;
    }

    private void applyDto(Course course, CourseCreateDTO dto) {
        course.setTitle(dto.getTitle());
        course.setCategory(dto.getCategory());
        course.setCover(dto.getCover());
        course.setIntro(dto.getIntro());
        course.setLessonCount(dto.getLessonCount() == null ? 0 : dto.getLessonCount());
        course.setPrice(dto.getPrice() == null ? BigDecimal.ZERO : dto.getPrice());
        course.setLocation(dto.getLocation());
        course.setTeacher(dto.getTeacher());
        course.setStartDate(dto.getStartDate());
        course.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
    }
}
