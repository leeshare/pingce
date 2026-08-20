package com.shaanxi.zhiping.controller;

import com.shaanxi.zhiping.common.PageResult;
import com.shaanxi.zhiping.common.Result;
import com.shaanxi.zhiping.dto.PaperQueryDTO;
import com.shaanxi.zhiping.entity.Paper;
import com.shaanxi.zhiping.exception.BusinessException;
import com.shaanxi.zhiping.service.PaperService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 管理后台 - 试卷管理 Controller
 *
 * 路径前缀：/admin/paper
 * 涵盖：真卷维护（CRUD + 题目组成）、试卷发布（状态切换）
 */
@RestController
@RequestMapping("/admin/paper")
public class AdminPaperController {

    @Resource
    private PaperService paperService;

    /**
     * 分页查询试卷
     * GET /api/admin/paper/list
     */
    @GetMapping("/list")
    public Result<PageResult<Paper>> list(PaperQueryDTO query) {
        return Result.success(paperService.listPapers(query));
    }

    /**
     * 试卷详情
     * GET /api/admin/paper/{id}
     */
    @GetMapping("/{id}")
    public Result<Paper> detail(@PathVariable Long id) {
        return Result.success(paperService.detail(id));
    }

    /**
     * 试卷预览（试卷元数据 + 按顺序的题目列表，含复合题子题）
     * GET /api/admin/paper/{id}/preview
     */
    @GetMapping("/{id}/preview")
    public Result<Map<String, Object>> preview(@PathVariable Long id) {
        Map<String, Object> data = paperService.preview(id);
        if (data == null) {
            throw new BusinessException("试卷不存在");
        }
        return Result.success(data);
    }

    /**
     * 新增试卷
     * POST /api/admin/paper
     */
    @PostMapping
    public Result<Long> create(@RequestBody Paper paper) {
        normalizePaper(paper);
        if (paper.getStatus() == null) {
            paper.setStatus(0);
        }
        return Result.success(paperService.create(paper));
    }

    /**
     * 更新试卷
     * PUT /api/admin/paper
     */
    @PutMapping
    public Result<Boolean> update(@RequestBody Paper paper) {
        if (paper.getId() == null) {
            throw new BusinessException("试卷ID不能为空");
        }
        normalizePaper(paper);
        return Result.success(paperService.update(paper));
    }

    /**
     * 删除试卷
     * DELETE /api/admin/paper/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(paperService.delete(id));
    }

    /**
     * 切换发布状态
     * PUT /api/admin/paper/{id}/status?status=1
     * status: 0草稿 1已发布
     */
    @PutMapping("/{id}/status")
    public Result<Boolean> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException("非法的发布状态");
        }
        return Result.success(paperService.updateStatus(id, status));
    }

    /**
     * 整理试卷字段：去除 question_ids 中空项、空白
     */
    private void normalizePaper(Paper paper) {
        if (paper.getQuestionIds() != null && !paper.getQuestionIds().isEmpty()) {
            String[] arr = paper.getQuestionIds().split(",");
            StringBuilder sb = new StringBuilder();
            for (String s : arr) {
                if (s != null && !s.trim().isEmpty()) {
                    if (sb.length() > 0) sb.append(",");
                    sb.append(s.trim());
                }
            }
            paper.setQuestionIds(sb.length() > 0 ? sb.toString() : null);
        }
    }
}
