package com.shaanxi.zhiping.service;

import com.shaanxi.zhiping.common.CacheConstants;
import com.shaanxi.zhiping.dto.HomeStatsVO;
import com.shaanxi.zhiping.mapper.UserMapper;
import com.shaanxi.zhiping.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 首页统计 Service（集成 Redis 缓存）
 *
 * 缓存策略：
 * - 首页统计数据：按用户缓存，TTL 5 分钟
 *   刷题数据不需要实时，5分钟延迟可接受，大幅降低 DB 压力
 */
@Slf4j
@Service
public class StatsService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private RedisUtil redisUtil;

    /**
     * 获取用户首页统计数据（带缓存）
     * Key: stats:home:{userId}，TTL 5 分钟
     */
    public HomeStatsVO getHomeStats(Long userId) {
        String cacheKey = CacheConstants.STATS_HOME_PREFIX + userId;
        HomeStatsVO cached = redisUtil.get(cacheKey);
        if (cached != null) {
            log.debug("首页统计命中缓存 userId={}", userId);
            return cached;
        }

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        int today = userMapper.countTodayPractice(userId, todayStart);
        int total = userMapper.countTotalPractice(userId);
        int correct = userMapper.countCorrectPractice(userId);
        int accuracy = total > 0 ? Math.round(correct * 100f / total) : 0;

        HomeStatsVO vo = new HomeStatsVO();
        vo.setToday(today);
        vo.setTotal(total);
        vo.setAccuracy(accuracy);

        redisUtil.set(cacheKey, vo, CacheConstants.TTL_STATS_HOME);
        return vo;
    }
}
