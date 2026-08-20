package com.shaanxi.zhiping.controller;

import com.shaanxi.zhiping.common.PageResult;
import com.shaanxi.zhiping.common.Result;
import com.shaanxi.zhiping.dto.CollegeCreateDTO;
import com.shaanxi.zhiping.dto.CollegeListVO;
import com.shaanxi.zhiping.dto.CollegeQueryDTO;
import com.shaanxi.zhiping.entity.College;
import com.shaanxi.zhiping.service.CollegeService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 管理后台 - 院校管理 Controller
 *
 * 路径前缀：/admin/college
 * 涵盖：分页查询、详情、新增、编辑、删除
 */
@RestController
@RequestMapping("/admin/college")
public class AdminCollegeController {

    @Resource
    private CollegeService collegeService;

    /**
     * 分页查询
     * GET /api/admin/college/list
     */
    @GetMapping("/list")
    public Result<PageResult<CollegeListVO>> list(CollegeQueryDTO query) {
        return Result.success(collegeService.listColleges(query));
    }

    /**
     * 详情
     * GET /api/admin/college/{id}
     */
    @GetMapping("/{id}")
    public Result<College> detail(@PathVariable Long id) {
        return Result.success(collegeService.getCollegeDetail(id));
    }

    /**
     * 新增
     * POST /api/admin/college
     */
    @PostMapping
    public Result<Long> create(@Validated @RequestBody CollegeCreateDTO dto) {
        return Result.success(collegeService.create(dto));
    }

    /**
     * 编辑
     * PUT /api/admin/college
     */
    @PutMapping
    public Result<Boolean> update(@Validated @RequestBody CollegeCreateDTO dto) {
        return Result.success(collegeService.update(dto));
    }

    /**
     * 删除
     * DELETE /api/admin/college/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(collegeService.delete(id));
    }
}
