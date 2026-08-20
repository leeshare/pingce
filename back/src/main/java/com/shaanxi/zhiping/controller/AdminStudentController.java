package com.shaanxi.zhiping.controller;

import com.shaanxi.zhiping.common.PageResult;
import com.shaanxi.zhiping.common.Result;
import com.shaanxi.zhiping.dto.StudentQueryDTO;
import com.shaanxi.zhiping.dto.StudentVO;
import com.shaanxi.zhiping.service.AdminStudentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 管理后台 - 学员管理接口
 * <p>数据来自微信小程序登录过的用户（t_user 表）
 */
@RestController
@RequestMapping("/admin/student")
public class AdminStudentController {

    @Resource
    private AdminStudentService adminStudentService;

    /**
     * 分页查询学员列表
     * GET /api/admin/student/page?keyword=&identity=&memberLevel=&province=&school=&page=&size=
     */
    @GetMapping("/page")
    public Result<PageResult<StudentVO>> page(StudentQueryDTO query) {
        return Result.success(adminStudentService.page(query));
    }
}
