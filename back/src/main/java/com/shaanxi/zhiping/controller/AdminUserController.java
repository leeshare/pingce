package com.shaanxi.zhiping.controller;

import com.shaanxi.zhiping.common.PageResult;
import com.shaanxi.zhiping.common.Result;
import com.shaanxi.zhiping.dto.AdminPermissionDTO;
import com.shaanxi.zhiping.dto.AdminPermissionOptionVO;
import com.shaanxi.zhiping.dto.AdminUserCreateDTO;
import com.shaanxi.zhiping.dto.AdminUserQueryDTO;
import com.shaanxi.zhiping.dto.AdminUserVO;
import com.shaanxi.zhiping.entity.AdminUser;
import com.shaanxi.zhiping.service.AdminAuthService;
import com.shaanxi.zhiping.service.AdminUserService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

/**
 * 管理后台 - 用户管理接口
 */
@RestController
@RequestMapping("/admin/user")
public class AdminUserController {

    @Resource
    private AdminUserService adminUserService;

    @Resource
    private AdminAuthService adminAuthService;

    /**
     * 分页查询管理员
     */
    @GetMapping("/page")
    public Result<PageResult<AdminUserVO>> page(AdminUserQueryDTO dto) {
        return Result.success(adminUserService.page(dto));
    }

    /**
     * 管理员详情
     */
    @GetMapping("/{id}")
    public Result<AdminUserVO> detail(@PathVariable Long id) {
        return Result.success(adminUserService.detail(id));
    }

    /**
     * 新增管理员
     */
    @PostMapping
    public Result<AdminUserVO> create(@RequestBody @Valid AdminUserCreateDTO dto, HttpServletRequest request) {
        return Result.success(adminUserService.create(dto, currentOperator(request)));
    }

    /**
     * 更新管理员
     */
    @PutMapping("/{id}")
    public Result<AdminUserVO> update(@PathVariable Long id,
                                       @RequestBody @Valid AdminUserCreateDTO dto,
                                       HttpServletRequest request) {
        return Result.success(adminUserService.update(id, dto, currentOperator(request)));
    }

    /**
     * 删除管理员
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id, HttpServletRequest request) {
        adminUserService.delete(id, currentOperator(request));
        return Result.success(true);
    }

    /**
     * 分配权限
     */
    @PutMapping("/{id}/permissions")
    public Result<AdminUserVO> assignPermissions(@PathVariable Long id,
                                                  @RequestBody @Valid AdminPermissionDTO dto,
                                                  HttpServletRequest request) {
        return Result.success(adminUserService.assignPermissions(id, dto, currentOperator(request)));
    }

    /**
     * 内置权限清单
     */
    @GetMapping("/permission-options")
    public Result<List<AdminPermissionOptionVO>> permissionOptions() {
        return Result.success(adminUserService.permissionOptions());
    }

    private AdminUser currentOperator(HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute("adminId");
        if (adminId == null) {
            return null;
        }
        return adminAuthService.loadById(adminId);
    }
}
