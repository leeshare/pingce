package com.shaanxi.zhiping.controller;

import com.shaanxi.zhiping.common.Result;
import com.shaanxi.zhiping.dto.HomeStatsVO;
import com.shaanxi.zhiping.service.StatsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 统计相关 Controller
 */
@RestController
@RequestMapping("/stats")
public class StatsController {

    @Resource
    private StatsService statsService;

    /**
     * 首页统计数据
     */
    @GetMapping("/home")
    public Result<HomeStatsVO> home(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        HomeStatsVO vo = statsService.getHomeStats(userId);
        return Result.success(vo);
    }
}
