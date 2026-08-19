package com.shaanxi.zhiping.controller;

import com.shaanxi.zhiping.common.Result;
import com.shaanxi.zhiping.dto.PracticeQueryDTO;
import com.shaanxi.zhiping.dto.PracticeQuestionVO;
import com.shaanxi.zhiping.service.PracticeService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 真题练习 Controller（小程序端）
 *
 * 路径前缀：/practice
 * 仅提供只读查询，与管理后台 {@link AdminQuestionController} 完全隔离：
 *  - 不暴露 bizSection / status 参数，由服务层强制为单招 + 已通过
 *  - 不返回管理字段（status / reviewerId / importBatchId / contentHash 等）
 *  - 复合题子题一次性嵌套返回
 *
 * 鉴权：所有接口需登录（JwtInterceptor 已配置），小程序 token 通过校验后可访问。
 */
@RestController
@RequestMapping("/practice")
public class PracticeController {

    @Resource
    private PracticeService practiceService;

    /**
     * 拉取一套真题
     * GET /api/practice/list?year=2026&categoryId=1
     *
     * @param query year + categoryId 必填
     * @return 题目列表（复合题子题已嵌套），按 sort、id 升序
     */
    @GetMapping("/list")
    public Result<List<PracticeQuestionVO>> list(PracticeQueryDTO query) {
        return Result.success(practiceService.listPaper(query));
    }

    /**
     * 单题详情（含复合题子题）
     * GET /api/practice/{id}
     *
     * 仅返回 status=2 已通过的题目，否则返回 null
     */
    @GetMapping("/{id}")
    public Result<PracticeQuestionVO> detail(@PathVariable Long id) {
        return Result.success(practiceService.detail(id));
    }
}
