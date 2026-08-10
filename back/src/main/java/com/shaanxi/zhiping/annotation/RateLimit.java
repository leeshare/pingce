package com.shaanxi.zhiping.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 接口限流注解
 *
 * 用法：标注在 Controller 方法上，自动按 IP 或 userId 限流
 *
 * 示例：
 *   @RateLimit(module = "login", limit = 5, window = 60)
 *   public Result<LoginVO> wxLogin(@RequestBody WxLoginDTO dto)
 *
 * 表示同一 IP 在 60 秒内最多调用 5 次登录接口
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    /** 业务模块名（用于区分限流维度） */
    String module();

    /** 窗口内允许的请求次数，默认 60 次/分钟 */
    int limit() default 60;

    /** 窗口大小（秒），默认 60 秒 */
    int window() default 60;

    /** 限流维度：ip=按客户端IP，user=按登录用户ID */
    String key() default "ip";
}
