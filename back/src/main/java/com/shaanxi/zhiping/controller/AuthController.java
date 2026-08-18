package com.shaanxi.zhiping.controller;

import com.shaanxi.zhiping.common.Result;
import com.shaanxi.zhiping.dto.IdentityChangeDTO;
import com.shaanxi.zhiping.dto.LoginVO;
import com.shaanxi.zhiping.dto.UserIdentityDTO;
import com.shaanxi.zhiping.dto.UserIdentityVO;
import com.shaanxi.zhiping.dto.WxLoginDTO;
import com.shaanxi.zhiping.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 认证 Controller（微信登录）
 */
@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Resource
    private AuthService authService;

    /**
     * 微信小程序登录
     */
    @PostMapping("/wx-login")
    public Result<LoginVO> wxLogin(@Validated @RequestBody WxLoginDTO dto) {
        log.info("微信登录请求, nickname={}", dto.getNickname());
        LoginVO vo = authService.wxLogin(dto);
        return Result.success(vo);
    }

    /**
     * 获取当前登录用户信息（验证 token 是否有效）
     */
    @GetMapping("/me")
    public Result<Long> me(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(userId);
    }

    /**
     * 退出登录
     * 清除 Redis 中的 Session，使 token 立即失效
     */
    @PostMapping("/logout")
    public Result<Boolean> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        String token = authHeader != null && authHeader.startsWith("Bearer ")
                ? authHeader.substring(7).trim() : null;
        boolean ok = authService.logout(token);
        return Result.success(ok);
    }

    @GetMapping("/identity")
    public Result<UserIdentityVO> getIdentity(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        UserIdentityVO vo = authService.getUserIdentity(userId);
        return Result.success(vo);
    }

    @PostMapping("/identity")
    public Result<Boolean> submitIdentity(@Validated @RequestBody UserIdentityDTO dto,
                                          HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        authService.updateUserIdentity(userId, dto);
        return Result.success(true);
    }

    @PostMapping("/identity-change")
    public Result<Boolean> submitIdentityChange(@Validated @RequestBody IdentityChangeDTO dto,
                                                 HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        authService.submitIdentityChange(userId, dto);
        return Result.success(true);
    }
}
