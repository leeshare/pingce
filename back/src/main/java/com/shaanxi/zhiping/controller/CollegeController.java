package com.shaanxi.zhiping.controller;

import com.shaanxi.zhiping.common.PageResult;
import com.shaanxi.zhiping.common.Result;
import com.shaanxi.zhiping.dto.CollegeListVO;
import com.shaanxi.zhiping.dto.CollegeQueryDTO;
import com.shaanxi.zhiping.entity.College;
import com.shaanxi.zhiping.service.CollegeService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 院校 Controller
 */
@RestController
@RequestMapping("/college")
public class CollegeController {

    @Resource
    private CollegeService collegeService;

    /**
     * 院校库分页列表
     * GET /api/college/list?keyword=&city=&nature=&type=&isDoubleHigh=&recommend=&page=&size=
     */
    @GetMapping("/list")
    public Result<PageResult<CollegeListVO>> list(CollegeQueryDTO query) {
        PageResult<CollegeListVO> result = collegeService.listColleges(query);
        return Result.success(result);
    }

    /**
     * 院校详情
     * GET /api/college/{id}
     */
    @GetMapping("/{id}")
    public Result<College> detail(@PathVariable Long id) {
        College college = collegeService.getCollegeDetail(id);
        return Result.success(college);
    }
}
