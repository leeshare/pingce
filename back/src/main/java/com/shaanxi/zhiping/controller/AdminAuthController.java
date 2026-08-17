package com.shaanxi.zhiping.controller;

import com.shaanxi.zhiping.common.Result;
import com.shaanxi.zhiping.dto.AdminLoginDTO;
import com.shaanxi.zhiping.dto.AdminLoginVO;
import com.shaanxi.zhiping.dto.AdminUserVO;
import com.shaanxi.zhiping.entity.AdminUser;
import com.shaanxi.zhiping.security.JwtUtils;
import com.shaanxi.zhiping.service.AdminAuthService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * 管理后台 - 认证接口
 */
@RestController
@RequestMapping("/admin")
public class AdminAuthController {

    @Resource
    private AdminAuthService adminAuthService;

    @Resource
    private JwtUtils jwtUtils;

    /**
     * 管理员登录
     */
    @PostMapping("/login")
    public Result<AdminLoginVO> login(@RequestBody @Valid AdminLoginDTO dto) {
        return Result.success(adminAuthService.login(dto));
    }

    /**
     * 获取当前登录管理员信息
     */
    @GetMapping("/info")
    public Result<AdminUserVO> info(HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute("adminId");
        return Result.success(adminAuthService.getCurrentAdmin(adminId));
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public Result<Boolean> logout(HttpServletRequest request) {
        String token = extractToken(request);
        return Result.success(adminAuthService.logout(token));
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            return null;
        }
        return header.substring("Bearer ".length()).trim();
    }
}
