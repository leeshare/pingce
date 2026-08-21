package com.shaanxi.zhiping.controller;

import com.shaanxi.zhiping.common.PageResult;
import com.shaanxi.zhiping.common.Result;
import com.shaanxi.zhiping.dto.PaperQueryDTO;
import com.shaanxi.zhiping.entity.Paper;
import com.shaanxi.zhiping.service.PaperService;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.beans.PropertyEditorSupport;

/**
 * 试卷 Controller
 */
@RestController
@RequestMapping("/paper")
public class PaperController {

    @Resource
    private PaperService paperService;

    /**
     * 兼容前端误传 "undefined"/"null" 字符串导致 Integer 绑定失败返回 400 的问题：
     * 将这些非法字符串统一当作 null 处理。
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(Integer.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                if (text == null || text.trim().isEmpty()
                        || "undefined".equalsIgnoreCase(text)
                        || "null".equalsIgnoreCase(text)) {
                    setValue(null);
                    return;
                }
                try {
                    setValue(Integer.valueOf(text.trim()));
                } catch (NumberFormatException e) {
                    setValue(null);
                }
            }
        });
    }

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
