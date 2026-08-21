package com.shaanxi.zhiping.controller;

import com.shaanxi.zhiping.common.Result;
import com.shaanxi.zhiping.dto.ExamRecordDetailVO;
import com.shaanxi.zhiping.dto.ExamRecordListVO;
import com.shaanxi.zhiping.service.ExamRecordService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 模考记录 Controller（小程序端）
 *
 * 路径前缀：/exam-record
 * 鉴权：需登录（JwtInterceptor 已注入 userId 到 request 属性）
 */
@RestController
@RequestMapping("/exam-record")
public class ExamRecordController {

    @Resource
    private ExamRecordService examRecordService;

    /**
     * 查询当前用户的历史模考记录列表
     * GET /api/exam-record/list
     *
     * @return 按交卷时间倒序
     */
    @GetMapping("/list")
    public Result<List<ExamRecordListVO>> list(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(examRecordService.list(userId));
    }

    /**
     * 查询单次考试作答详情
     * GET /api/exam-record/{recordId}
     *
     * @param recordId 考试记录ID
     * @return 详情 VO；记录不存在或不属于该用户时返回 null
     */
    @GetMapping("/{recordId}")
    public Result<ExamRecordDetailVO> detail(@PathVariable Long recordId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(examRecordService.detail(recordId, userId));
    }
}
