package com.shaanxi.zhiping.controller;

import com.shaanxi.zhiping.common.PageResult;
import com.shaanxi.zhiping.common.Result;
import com.shaanxi.zhiping.dto.QuestionQueryDTO;
import com.shaanxi.zhiping.entity.Question;
import com.shaanxi.zhiping.service.QuestionService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 题目 Controller
 */
@RestController
@RequestMapping("/question")
public class QuestionController {

    @Resource
    private QuestionService questionService;

    /**
     * 分页查询题目
     * GET /api/question/list
     */
    @GetMapping("/list")
    public Result<PageResult<Question>> list(QuestionQueryDTO query) {
        return Result.success(questionService.listQuestions(query));
    }

    /**
     * 题目详情
     * GET /api/question/{id}
     */
    @GetMapping("/{id}")
    public Result<Question> detail(@PathVariable Long id) {
        return Result.success(questionService.detail(id));
    }

    /**
     * 查询复合题子题
     * GET /api/question/children?parentId=
     */
    @GetMapping("/children")
    public Result<List<Question>> children(@RequestParam Long parentId) {
        return Result.success(questionService.listChildren(parentId));
    }

    /**
     * 新增题目
     * POST /api/question
     */
    @PostMapping
    public Result<Long> create(@RequestBody Question question) {
        return Result.success(questionService.create(question));
    }

    /**
     * 更新题目
     * PUT /api/question
     */
    @PutMapping
    public Result<Boolean> update(@RequestBody Question question) {
        return Result.success(questionService.update(question));
    }

    /**
     * 删除题目
     * DELETE /api/question/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(questionService.delete(id));
    }
}
