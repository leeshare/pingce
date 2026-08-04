package com.shaanxi.zhiping.controller;

import com.shaanxi.zhiping.common.Result;
import com.shaanxi.zhiping.dto.LoginVO;
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
}
