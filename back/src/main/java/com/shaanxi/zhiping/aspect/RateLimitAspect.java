package com.shaanxi.zhiping.aspect;

import com.shaanxi.zhiping.annotation.RateLimit;
import com.shaanxi.zhiping.common.Result;
import com.shaanxi.zhiping.common.ResultCode;
import com.shaanxi.zhiping.util.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 限流切面
 * 拦截标注了 @RateLimit 的方法，按 IP 或 userId 进行限流
 */
@Slf4j
@Aspect
@Component
public class RateLimitAspect {

    @Resource
    private RateLimiter rateLimiter;

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        String key = resolveKey(rateLimit.key());
        boolean allowed = rateLimiter.tryAcquire(
                rateLimit.module(), key, rateLimit.limit(), rateLimit.window());
        if (!allowed) {
            log.warn("接口被限流 module={} key={}", rateLimit.module(), key);
            return Result.fail(ResultCode.FAIL.getCode(),
                    "请求过于频繁，请" + rateLimit.window() + "秒后重试");
        }
        return joinPoint.proceed();
    }

    /**
     * 解析限流 Key
     */
    private String resolveKey(String keyType) {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return "unknown";
        }
        HttpServletRequest request = attrs.getRequest();

        if ("user".equals(keyType)) {
            Object userId = request.getAttribute("userId");
            return userId == null ? getClientIp(request) : "u_" + userId;
        }
        // 默认按 IP
        return getClientIp(request);
    }

    /**
     * 获取客户端真实 IP（穿透代理）
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多级代理时取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip == null ? "unknown" : ip;
    }
}
