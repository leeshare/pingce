package com.shaanxi.zhiping.controller;

import com.shaanxi.zhiping.common.PageResult;
import com.shaanxi.zhiping.common.Result;
import com.shaanxi.zhiping.dto.CourseCreateDTO;
import com.shaanxi.zhiping.dto.CourseQueryDTO;
import com.shaanxi.zhiping.entity.Course;
import com.shaanxi.zhiping.service.CourseService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 管理后台 - 课程管理 Controller
 *
 * 路径前缀：/admin/course
 * 涵盖：分页查询、详情、新增、编辑、删除、上下架
 */
@RestController
@RequestMapping("/admin/course")
public class AdminCourseController {

    @Resource
    private CourseService courseService;

    /**
     * 分页查询
     * GET /api/admin/course/list
     */
    @GetMapping("/list")
    public Result<PageResult<Course>> list(CourseQueryDTO query) {
        return Result.success(courseService.page(query));
    }

    /**
     * 详情
     */
    @GetMapping("/{id}")
    public Result<Course> detail(@PathVariable Long id) {
        return Result.success(courseService.detail(id));
    }

    /**
     * 新增
     */
    @PostMapping
    public Result<Long> create(@Validated @RequestBody CourseCreateDTO dto) {
        return Result.success(courseService.create(dto));
    }

    /**
     * 编辑
     */
    @PutMapping
    public Result<Boolean> update(@Validated @RequestBody CourseCreateDTO dto) {
        return Result.success(courseService.update(dto));
    }

    /**
     * 删除
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(courseService.delete(id));
    }

    /**
     * 上架/下架
     * PUT /api/admin/course/{id}/status?status=1
     */
    @PutMapping("/{id}/status")
    public Result<Boolean> toggleStatus(@PathVariable Long id, @RequestParam Integer status) {
        return Result.success(courseService.toggleStatus(id, status));
    }
}
