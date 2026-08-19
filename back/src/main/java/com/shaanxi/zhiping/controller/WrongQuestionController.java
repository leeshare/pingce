package com.shaanxi.zhiping.controller;

import com.shaanxi.zhiping.common.Result;
import com.shaanxi.zhiping.service.WrongQuestionService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 错题本 Controller（小程序端）
 *
 * 路径前缀：/wrong
 * 鉴权：需登录（JwtInterceptor 已注入 userId 到 request 属性）
 *
 * 当前仅提供上报接口，错题列表/掌握标记等后续按需扩展。
 */
@RestController
@RequestMapping("/wrong")
public class WrongQuestionController {

    @Resource
    private WrongQuestionService wrongQuestionService;

    /**
     * 上报一道错题
     * POST /api/wrong/report?questionId=123
     *
     * 幂等：重复上报同题会累加 wrong_count，并重置 mastered=0
     *
     * @param questionId 题目ID（复合题为子题ID）
     */
    @PostMapping("/report")
    public Result<Boolean> report(@RequestParam Long questionId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        boolean ok = wrongQuestionService.report(userId, questionId);
        return Result.success(ok);
    }
}
