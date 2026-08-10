package com.shaanxi.zhiping.util;

import com.shaanxi.zhiping.common.CacheConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * 基于 Redis 的限流器
 *
 * 实现：固定窗口计数器算法
 * 原理：在窗口时间内（默认60秒）累计请求数，超过阈值则拒绝
 *
 * 适用场景：
 * - 接口防刷（如登录、提交答案）
 * - IP 限流（防止爬虫批量拉题）
 * - 用户级限流（防止单用户刷接口）
 */
@Slf4j
@Component
public class RateLimiter {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 尝试获取访问许可（固定窗口算法）
     *
     * @param module        业务模块名，如 "login"、"submit"
     * @param key           限流维度，如 userId 或 IP
     * @param limit         窗口内允许的请求次数
     * @param windowSeconds 窗口大小（秒）
     * @return true=允许访问, false=已被限流
     */
    public boolean tryAcquire(String module, String key, int limit, long windowSeconds) {
        String redisKey = CacheConstants.RATE_LIMIT_PREFIX + module + ":" + key;
        try {
            Long count = redisTemplate.opsForValue().increment(redisKey);
            if (count != null && count == 1) {
                // 第一次请求才设置过期时间，保证窗口完整
                redisTemplate.expire(redisKey, windowSeconds, TimeUnit.SECONDS);
            }
            if (count != null && count > limit) {
                log.warn("触发限流 module={} key={} count={}/{}", module, key, count, limit);
                return false;
            }
            return true;
        } catch (Exception e) {
            // Redis 不可用时降级放行，避免阻断业务
            log.error("限流器 Redis 异常，降级放行 module={} key={}", module, key, e);
            return true;
        }
    }

    /**
     * 尝试获取访问许可（默认窗口 60 秒）
     */
    public boolean tryAcquire(String module, String key, int limit) {
        return tryAcquire(module, key, limit, CacheConstants.TTL_RATE_LIMIT_DEFAULT);
    }

    /**
     * 查询当前剩余可用次数
     */
    public long getRemaining(String module, String key, int limit) {
        String redisKey = CacheConstants.RATE_LIMIT_PREFIX + module + ":" + key;
        try {
            Object val = redisTemplate.opsForValue().get(redisKey);
            long count = val == null ? 0 : Long.parseLong(val.toString());
            return Math.max(0, limit - count);
        } catch (Exception e) {
            log.error("查询剩余次数失败 module={} key={}", module, key, e);
            return limit;
        }
    }

    /**
     * 重置计数器（管理员手动解除限流）
     */
    public boolean reset(String module, String key) {
        String redisKey = CacheConstants.RATE_LIMIT_PREFIX + module + ":" + key;
        try {
            return Boolean.TRUE.equals(redisTemplate.delete(redisKey));
        } catch (Exception e) {
            log.error("重置限流器失败 module={} key={}", module, key, e);
            return false;
        }
    }
}
