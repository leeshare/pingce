package com.shaanxi.zhiping.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shaanxi.zhiping.common.Result;
import com.shaanxi.zhiping.common.ResultCode;
import com.shaanxi.zhiping.config.JwtConfig;
import com.shaanxi.zhiping.service.AdminAuthService;
import com.shaanxi.zhiping.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

/**
 * JWT 拦截器
 *
 * 校验流程：
 * 1. 提取 Authorization 头中的 token
 * 2. 校验 JWT 签名与过期时间
 * 3. 区分小程序 / 管理后台 token，校验对应 Redis Session
 */
@Slf4j
@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Resource
    private JwtUtils jwtUtils;

    @Resource
    private JwtConfig jwtConfig;

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private AuthService authService;

    @Resource
    private AdminAuthService adminAuthService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // OPTIONS 预检放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String authHeader = request.getHeader(jwtConfig.getHeader());
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith(jwtConfig.getPrefix())) {
            returnUnauthorized(response, ResultCode.UNAUTHORIZED);
            return false;
        }

        String token = authHeader.substring(jwtConfig.getPrefix().length()).trim();
        if (!jwtUtils.validateToken(token)) {
            returnUnauthorized(response, ResultCode.TOKEN_INVALID);
            return false;
        }

        // 区分管理员 token 与小程序 token
        if (jwtUtils.isAdminToken(token)) {
            if (!adminAuthService.isSessionValid(token)) {
                log.debug("管理员 token Session 已失效");
                returnUnauthorized(response, ResultCode.TOKEN_INVALID);
                return false;
            }
            Long adminId = jwtUtils.getAdminIdFromToken(token);
            request.setAttribute("adminId", adminId);
            request.setAttribute("username", jwtUtils.getUsernameFromToken(token));
            return true;
        }

        // 小程序 token 校验
        if (!authService.isSessionValid(token)) {
            log.debug("token Session 已失效（已退出登录或过期）");
            returnUnauthorized(response, ResultCode.TOKEN_INVALID);
            return false;
        }

        // 将 userId 放入 request 属性，后续 Controller 可直接获取
        Long userId = jwtUtils.getUserIdFromToken(token);
        request.setAttribute("userId", userId);
        request.setAttribute("openid", jwtUtils.getOpenidFromToken(token));
        return true;
    }

    private void returnUnauthorized(HttpServletResponse response, ResultCode resultCode) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        try (PrintWriter writer = response.getWriter()) {
            writer.write(objectMapper.writeValueAsString(Result.fail(resultCode)));
            writer.flush();
        }
    }
}
