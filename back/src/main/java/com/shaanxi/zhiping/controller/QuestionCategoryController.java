package com.shaanxi.zhiping.controller;

import com.shaanxi.zhiping.common.Result;
import com.shaanxi.zhiping.entity.QuestionCategory;
import com.shaanxi.zhiping.service.QuestionCategoryService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 题库分类 Controller
 */
@RestController
@RequestMapping("/questionCategory")
public class QuestionCategoryController {

    @Resource
    private QuestionCategoryService questionCategoryService;

    /**
     * 查询全部分类
     * GET /api/questionCategory/list
     */
    @GetMapping("/list")
    public Result<List<QuestionCategory>> list() {
        return Result.success(questionCategoryService.listAll());
    }

    /**
     * 按父分类查询子分类
     * GET /api/questionCategory/children?parentId=
     */
    @GetMapping("/children")
    public Result<List<QuestionCategory>> children(@RequestParam Long parentId) {
        return Result.success(questionCategoryService.listByParent(parentId));
    }

    /**
     * 分类详情
     * GET /api/questionCategory/{id}
     */
    @GetMapping("/{id}")
    public Result<QuestionCategory> detail(@PathVariable Long id) {
        return Result.success(questionCategoryService.detail(id));
    }

    /**
     * 新增分类
     * POST /api/questionCategory
     */
    @PostMapping
    public Result<Long> create(@RequestBody QuestionCategory category) {
        return Result.success(questionCategoryService.create(category));
    }

    /**
     * 更新分类
     * PUT /api/questionCategory
     */
    @PutMapping
    public Result<Boolean> update(@RequestBody QuestionCategory category) {
        return Result.success(questionCategoryService.update(category));
    }

    /**
     * 删除分类
     * DELETE /api/questionCategory/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(questionCategoryService.delete(id));
    }
}
