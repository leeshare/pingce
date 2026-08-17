package com.shaanxi.zhiping.controller;

import com.shaanxi.zhiping.common.PageResult;
import com.shaanxi.zhiping.common.Result;
import com.shaanxi.zhiping.dto.QuestionCreateDTO;
import com.shaanxi.zhiping.dto.QuestionImportBatchQueryDTO;
import com.shaanxi.zhiping.dto.QuestionImportDTO;
import com.shaanxi.zhiping.dto.QuestionImportResultVO;
import com.shaanxi.zhiping.dto.QuestionProofreadStatVO;
import com.shaanxi.zhiping.dto.QuestionQueryDTO;
import com.shaanxi.zhiping.dto.QuestionReviewDTO;
import com.shaanxi.zhiping.entity.Question;
import com.shaanxi.zhiping.entity.QuestionImportBatch;
import com.shaanxi.zhiping.service.AdminQuestionService;
import com.shaanxi.zhiping.service.QuestionCategoryService;
import com.shaanxi.zhiping.entity.QuestionCategory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 管理后台 - 题库中心 Controller
 *
 * 路径前缀：/admin/question
 * 涵盖 5 大功能：
 *  1. 试题录入   POST /admin/question           create
 *                 PUT  /admin/question           update
 *  2. 批量导入   POST /admin/question/import    importExcel
 *                 GET  /admin/question/batch    listBatches
 *                 GET  /admin/question/batch/{batchId} getBatch
 *  3. 试题校对   GET  /admin/question/proofread  listForProofread
 *                 PUT  /admin/question/proofread proofreadSave
 *  4. 试题编辑   GET  /admin/question/list       listForEdit
 *                 PUT  /admin/question            update（共用）
 *                 DELETE /admin/question/{id}    delete
 *                 DELETE /admin/question/batch-delete  deleteBatch
 *  5. 试题审核   POST /admin/question/review     review
 *
 * 通用：
 *  - GET  /admin/question/{id}      detail
 *  - GET  /admin/question/stat      stat
 *  - GET  /admin/question/categories listCategories
 */
@RestController
@RequestMapping("/admin/question")
public class AdminQuestionController {

    @Resource
    private AdminQuestionService adminQuestionService;

    @Resource
    private QuestionCategoryService questionCategoryService;

    // ==================== 1. 试题录入 ====================

    @PostMapping
    public Result<Long> create(@RequestBody QuestionCreateDTO dto) {
        return Result.success(adminQuestionService.create(dto));
    }

    @PutMapping
    public Result<Boolean> update(@RequestBody QuestionCreateDTO dto) {
        return Result.success(adminQuestionService.update(dto));
    }

    // ==================== 2. 批量导入 ====================

    @PostMapping("/import")
    public Result<QuestionImportResultVO> importExcel(
            @RequestParam("file") MultipartFile file,
            QuestionImportDTO params,
            HttpServletRequest request) {
        Long operatorId = (Long) request.getAttribute("userId");
        return Result.success(adminQuestionService.importExcel(file, params, operatorId));
    }

    @GetMapping("/batch")
    public Result<PageResult<QuestionImportBatch>> listBatches(QuestionImportBatchQueryDTO query) {
        return Result.success(adminQuestionService.listBatches(query));
    }

    @GetMapping("/batch/{batchId}")
    public Result<QuestionImportBatch> getBatch(@PathVariable String batchId) {
        return Result.success(adminQuestionService.getBatch(batchId));
    }

    // ==================== 3. 试题校对 ====================

    @GetMapping("/proofread")
    public Result<PageResult<Question>> listForProofread(QuestionQueryDTO query) {
        return Result.success(adminQuestionService.listForProofread(query));
    }

    @PutMapping("/proofread")
    public Result<Boolean> proofreadSave(@RequestBody QuestionCreateDTO dto) {
        return Result.success(adminQuestionService.proofreadSave(dto));
    }

    // ==================== 4. 试题编辑 ====================

    @GetMapping("/list")
    public Result<PageResult<Question>> listForEdit(QuestionQueryDTO query) {
        return Result.success(adminQuestionService.listForEdit(query));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(adminQuestionService.delete(id));
    }

    @DeleteMapping("/batch-delete")
    public Result<Integer> deleteBatch(@RequestParam List<Long> ids) {
        return Result.success(adminQuestionService.deleteBatch(ids));
    }

    // ==================== 5. 试题审核 ====================

    @PostMapping("/review")
    public Result<Integer> review(@Validated @RequestBody QuestionReviewDTO dto,
                                  HttpServletRequest request) {
        Long reviewerId = (Long) request.getAttribute("userId");
        return Result.success(adminQuestionService.review(dto, reviewerId));
    }

    // ==================== 通用 ====================

    @GetMapping("/{id}")
    public Result<Question> detail(@PathVariable Long id) {
        return Result.success(adminQuestionService.detail(id));
    }

    /**
     * 查询复合题(type=7)的子题列表
     * 用于前端在详情弹窗中展开复合题子题
     */
    @GetMapping("/{id}/children")
    public Result<List<Question>> listChildren(@PathVariable Long id) {
        return Result.success(adminQuestionService.listChildren(id));
    }

    @GetMapping("/stat")
    public Result<QuestionProofreadStatVO> stat() {
        return Result.success(adminQuestionService.stat());
    }

    @GetMapping("/categories")
    public Result<List<QuestionCategory>> listCategories() {
        return Result.success(questionCategoryService.listAll());
    }
}
