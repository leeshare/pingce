package com.shaanxi.zhiping.controller;

import com.shaanxi.zhiping.common.PageResult;
import com.shaanxi.zhiping.common.Result;
import com.shaanxi.zhiping.dto.PaperQueryDTO;
import com.shaanxi.zhiping.entity.Paper;
import com.shaanxi.zhiping.service.PaperService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 试卷 Controller
 */
@RestController
@RequestMapping("/paper")
public class PaperController {

    @Resource
    private PaperService paperService;

    /**
     * 分页查询试卷
     * GET /api/paper/list
     */
    @GetMapping("/list")
    public Result<PageResult<Paper>> list(PaperQueryDTO query) {
        return Result.success(paperService.listPapers(query));
    }

    /**
     * 试卷详情
     * GET /api/paper/{id}
     */
    @GetMapping("/{id}")
    public Result<Paper> detail(@PathVariable Long id) {
        return Result.success(paperService.detail(id));
    }

    /**
     * 新增试卷
     * POST /api/paper
     */
    @PostMapping
    public Result<Long> create(@RequestBody Paper paper) {
        return Result.success(paperService.create(paper));
    }

    /**
     * 更新试卷
     * PUT /api/paper
     */
    @PutMapping
    public Result<Boolean> update(@RequestBody Paper paper) {
        return Result.success(paperService.update(paper));
    }

    /**
     * 删除试卷
     * DELETE /api/paper/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(paperService.delete(id));
    }
}
