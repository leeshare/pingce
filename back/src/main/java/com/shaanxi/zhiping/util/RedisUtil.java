package com.shaanxi.zhiping.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis 操作工具类
 * 封装常用操作，统一异常处理（Redis 不可用时降级返回 null，不抛异常阻断业务）
 */
@Slf4j
@Component
public class RedisUtil {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    // ==================== 基础读写 ====================

    /**
     * 写入缓存（带过期时间，秒）
     */
    public void set(String key, Object value, long timeoutSeconds) {
        try {
            redisTemplate.opsForValue().set(key, value, timeoutSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Redis set 失败 key={}", key, e);
        }
    }

    /**
     * 写入缓存（不过期）
     */
    public void set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
        } catch (Exception e) {
            log.error("Redis set 失败 key={}", key, e);
        }
    }

    /**
     * 读取缓存
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        try {
            return (T) redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("Redis get 失败 key={}", key, e);
            return null;
        }
    }

    /**
     * 读取缓存为字符串
     */
    public String getString(String key) {
        try {
            Object obj = redisTemplate.opsForValue().get(key);
            return obj == null ? null : obj.toString();
        } catch (Exception e) {
            log.error("Redis getString 失败 key={}", key, e);
            return null;
        }
    }

    /**
     * 删除单个 key
     */
    public boolean delete(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.delete(key));
        } catch (Exception e) {
            log.error("Redis delete 失败 key={}", key, e);
            return false;
        }
    }

    /**
     * 批量删除
     */
    public long delete(Collection<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return 0;
        }
        try {
            Long count = redisTemplate.delete(keys);
            return count == null ? 0 : count;
        } catch (Exception e) {
            log.error("Redis batch delete 失败 keys={}", keys, e);
            return 0;
        }
    }

    /**
     * 按前缀模糊删除（慎用，scan 避免阻塞）
     */
    public long deleteByPrefix(String prefix) {
        try {
            Set<String> keys = redisTemplate.keys(prefix + "*");
            if (keys == null || keys.isEmpty()) {
                return 0;
            }
            Long count = redisTemplate.delete(keys);
            return count == null ? 0 : count;
        } catch (Exception e) {
            log.error("Redis deleteByPrefix 失败 prefix={}", prefix, e);
            return 0;
        }
    }

    /**
     * 判断 key 是否存在
     */
    public boolean hasKey(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error("Redis hasKey 失败 key={}", key, e);
            return false;
        }
    }

    /**
     * 设置过期时间
     */
    public boolean expire(String key, long timeoutSeconds) {
        try {
            return Boolean.TRUE.equals(redisTemplate.expire(key, timeoutSeconds, TimeUnit.SECONDS));
        } catch (Exception e) {
            log.error("Redis expire 失败 key={}", key, e);
            return false;
        }
    }

    // ==================== 计数器（限流用） ====================

    /**
     * 自增 1，返回自增后的值
     */
    public long incr(String key, long timeoutSeconds) {
        try {
            Long count = redisTemplate.opsForValue().increment(key);
            if (count != null && count == 1) {
                // 第一次写入才设置过期时间
                redisTemplate.expire(key, timeoutSeconds, TimeUnit.SECONDS);
            }
            return count == null ? 0 : count;
        } catch (Exception e) {
            log.error("Redis incr 失败 key={}", key, e);
            return 0;
        }
    }

    /**
     * 自减 1
     */
    public long decr(String key) {
        try {
            Long count = redisTemplate.opsForValue().decrement(key);
            return count == null ? 0 : count;
        } catch (Exception e) {
            log.error("Redis decr 失败 key={}", key, e);
            return 0;
        }
    }

    // ==================== List 操作 ====================

    /**
     * 写入 List 缓存
     */
    public <T> void setList(String key, List<T> list, long timeoutSeconds) {
        try {
            redisTemplate.opsForValue().set(key, list, timeoutSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Redis setList 失败 key={}", key, e);
        }
    }

    /**
     * 读取 List 缓存
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> getList(String key) {
        try {
            Object obj = redisTemplate.opsForValue().get(key);
            return obj == null ? null : (List<T>) obj;
        } catch (Exception e) {
            log.error("Redis getList 失败 key={}", key, e);
            return null;
        }
    }
}
