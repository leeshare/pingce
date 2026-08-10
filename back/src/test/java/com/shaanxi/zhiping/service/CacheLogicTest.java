package com.shaanxi.zhiping.service;

import com.shaanxi.zhiping.common.CacheConstants;
import com.shaanxi.zhiping.dto.HomeStatsVO;
import com.shaanxi.zhiping.entity.College;
import com.shaanxi.zhiping.entity.Paper;
import com.shaanxi.zhiping.entity.User;
import com.shaanxi.zhiping.mapper.CollegeMapper;
import com.shaanxi.zhiping.mapper.PaperMapper;
import com.shaanxi.zhiping.mapper.UserMapper;
import com.shaanxi.zhiping.security.JwtUtils;
import com.shaanxi.zhiping.util.RedisUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 缓存逻辑单元测试（纯 Mockito，不依赖 Spring 容器/DB/Redis）
 *
 * 覆盖 Session 缓存 + 热点数据缓存的核心分支：
 * 1. 命中缓存：直接返回，不查 DB
 * 2. 未命中缓存：查 DB，回写缓存
 * 3. DB 为空：不写缓存，返回 null
 * 4. 更新/删除：清除对应缓存
 */
@ExtendWith(MockitoExtension.class)
class CacheLogicTest {

    @Nested
    @DisplayName("PaperService 试卷详情缓存")
    class PaperServiceCacheTest {

        @Mock
        private PaperMapper paperMapper;

        @Mock
        private RedisUtil redisUtil;

        @InjectMocks
        private PaperService paperService;

        @Test
        @DisplayName("命中缓存：直接返回，不查 DB")
        void detail_cacheHit_shouldNotQueryDb() {
            Long id = 10L;
            Paper cached = new Paper();
            cached.setId(id);
            cached.setTitle("缓存中的试卷");
            when(redisUtil.get(CacheConstants.PAPER_DETAIL_PREFIX + id)).thenReturn(cached);

            Paper result = paperService.detail(id);

            assertNotNull(result);
            assertEquals(id, result.getId());
            assertEquals("缓存中的试卷", result.getTitle());
            verify(redisUtil, times(1)).get(anyString());
            verify(paperMapper, never()).selectById(anyLong());
            verify(redisUtil, never()).set(anyString(), any(), anyLong());
        }

        @Test
        @DisplayName("未命中缓存：查 DB 并回写缓存")
        void detail_cacheMiss_shouldQueryDbAndWriteCache() {
            Long id = 11L;
            Paper dbPaper = new Paper();
            dbPaper.setId(id);
            dbPaper.setTitle("DB中的试卷");
            dbPaper.setDuration(90);
            when(redisUtil.get(CacheConstants.PAPER_DETAIL_PREFIX + id)).thenReturn(null);
            when(paperMapper.selectById(id)).thenReturn(dbPaper);

            Paper result = paperService.detail(id);

            assertNotNull(result);
            assertEquals(id, result.getId());
            assertEquals("DB中的试卷", result.getTitle());
            verify(paperMapper, times(1)).selectById(id);
            verify(redisUtil, times(1)).set(
                    eq(CacheConstants.PAPER_DETAIL_PREFIX + id),
                    eq(dbPaper),
                    eq(CacheConstants.TTL_PAPER_DETAIL));
        }

        @Test
        @DisplayName("DB 为空：不写缓存，返回 null")
        void detail_dbEmpty_shouldNotWriteCache() {
            Long id = 12L;
            when(redisUtil.get(CacheConstants.PAPER_DETAIL_PREFIX + id)).thenReturn(null);
            when(paperMapper.selectById(id)).thenReturn(null);

            Paper result = paperService.detail(id);

            assertNull(result);
            verify(paperMapper, times(1)).selectById(id);
            verify(redisUtil, never()).set(anyString(), any(), anyLong());
        }

        @Test
        @DisplayName("更新试卷成功：清除对应缓存")
        void update_success_shouldDeleteCache() {
            Paper p = new Paper();
            p.setId(20L);
            p.setTitle("更新后的标题");
            when(paperMapper.updateById(p)).thenReturn(1);

            boolean ok = paperService.update(p);

            assertTrue(ok);
            verify(redisUtil, times(1)).delete(CacheConstants.PAPER_DETAIL_PREFIX + 20L);
        }

        @Test
        @DisplayName("更新试卷失败（DB返回0）：不清除缓存")
        void update_failed_shouldNotDeleteCache() {
            Paper p = new Paper();
            p.setId(21L);
            when(paperMapper.updateById(p)).thenReturn(0);

            boolean ok = paperService.update(p);

            assertFalse(ok);
            verify(redisUtil, never()).delete(anyString());
        }

        @Test
        @DisplayName("删除试卷成功：清除对应缓存")
        void delete_success_shouldDeleteCache() {
            Long id = 30L;
            when(paperMapper.deleteById(id)).thenReturn(1);

            boolean ok = paperService.delete(id);

            assertTrue(ok);
            verify(redisUtil, times(1)).delete(CacheConstants.PAPER_DETAIL_PREFIX + id);
        }
    }

    @Nested
    @DisplayName("CollegeService 院校详情缓存")
    class CollegeServiceCacheTest {

        @Mock
        private CollegeMapper collegeMapper;

        @Mock
        private RedisUtil redisUtil;

        @InjectMocks
        private CollegeService collegeService;

        @Test
        @DisplayName("命中缓存：直接返回，不查 DB")
        void detail_cacheHit_shouldNotQueryDb() {
            Long id = 100L;
            College cached = new College();
            cached.setId(id);
            cached.setName("缓存中的院校");
            when(redisUtil.get(CacheConstants.COLLEGE_DETAIL_PREFIX + id)).thenReturn(cached);

            College result = collegeService.getCollegeDetail(id);

            assertNotNull(result);
            assertEquals(id, result.getId());
            verify(collegeMapper, never()).selectById(anyLong());
            verify(redisUtil, never()).set(anyString(), any(), anyLong());
        }

        @Test
        @DisplayName("未命中缓存：查 DB 并回写缓存（TTL=1小时）")
        void detail_cacheMiss_shouldQueryDbAndWriteCache() {
            Long id = 101L;
            College dbCollege = new College();
            dbCollege.setId(id);
            dbCollege.setName("DB中的院校");
            when(redisUtil.get(CacheConstants.COLLEGE_DETAIL_PREFIX + id)).thenReturn(null);
            when(collegeMapper.selectById(id)).thenReturn(dbCollege);

            College result = collegeService.getCollegeDetail(id);

            assertNotNull(result);
            assertEquals("DB中的院校", result.getName());
            verify(collegeMapper, times(1)).selectById(id);
            verify(redisUtil, times(1)).set(
                    eq(CacheConstants.COLLEGE_DETAIL_PREFIX + id),
                    eq(dbCollege),
                    eq(CacheConstants.TTL_COLLEGE_DETAIL));
            // 校验 TTL 确实是 1 小时
            assertEquals(3600L, CacheConstants.TTL_COLLEGE_DETAIL);
        }

        @Test
        @DisplayName("DB 为空：不写缓存，返回 null")
        void detail_dbEmpty_shouldNotWriteCache() {
            Long id = 102L;
            when(redisUtil.get(CacheConstants.COLLEGE_DETAIL_PREFIX + id)).thenReturn(null);
            when(collegeMapper.selectById(id)).thenReturn(null);

            College result = collegeService.getCollegeDetail(id);

            assertNull(result);
            verify(redisUtil, never()).set(anyString(), any(), anyLong());
        }
    }

    @Nested
    @DisplayName("StatsService 首页统计缓存")
    class StatsServiceCacheTest {

        @Mock
        private UserMapper userMapper;

        @Mock
        private RedisUtil redisUtil;

        @InjectMocks
        private StatsService statsService;

        @Test
        @DisplayName("命中缓存：直接返回，不查 DB")
        void homeStats_cacheHit_shouldNotQueryDb() {
            Long userId = 1L;
            HomeStatsVO cached = new HomeStatsVO();
            cached.setToday(5);
            cached.setTotal(100);
            cached.setAccuracy(80);
            when(redisUtil.get(CacheConstants.STATS_HOME_PREFIX + userId)).thenReturn(cached);

            HomeStatsVO result = statsService.getHomeStats(userId);

            assertNotNull(result);
            assertEquals(5, result.getToday());
            assertEquals(100, result.getTotal());
            assertEquals(80, result.getAccuracy());
            verify(userMapper, never()).countTodayPractice(anyLong(), any());
            verify(userMapper, never()).countTotalPractice(anyLong());
            verify(redisUtil, never()).set(anyString(), any(), anyLong());
        }

        @Test
        @DisplayName("未命中缓存：查 DB 计算并回写缓存（TTL=5分钟）")
        void homeStats_cacheMiss_shouldQueryDbAndWriteCache() {
            Long userId = 2L;
            when(redisUtil.get(CacheConstants.STATS_HOME_PREFIX + userId)).thenReturn(null);
            when(userMapper.countTodayPractice(eq(userId), any(LocalDateTime.class))).thenReturn(3);
            when(userMapper.countTotalPractice(userId)).thenReturn(50);
            when(userMapper.countCorrectPractice(userId)).thenReturn(40);

            HomeStatsVO result = statsService.getHomeStats(userId);

            assertNotNull(result);
            assertEquals(3, result.getToday());
            assertEquals(50, result.getTotal());
            // 正确率 = 40/100*100 = 80
            assertEquals(80, result.getAccuracy());
            verify(userMapper, times(1)).countTodayPractice(eq(userId), any());
            verify(userMapper, times(1)).countTotalPractice(userId);
            verify(userMapper, times(1)).countCorrectPractice(userId);
            verify(redisUtil, times(1)).set(
                    eq(CacheConstants.STATS_HOME_PREFIX + userId),
                    any(HomeStatsVO.class),
                    eq(CacheConstants.TTL_STATS_HOME));
            // TTL 应为 5 分钟 = 300 秒
            assertEquals(300L, CacheConstants.TTL_STATS_HOME);
        }

        @Test
        @DisplayName("未命中缓存且总刷题数为0：正确率应为0，避免除零异常")
        void homeStats_zeroTotal_accuracyShouldBeZero() {
            Long userId = 3L;
            when(redisUtil.get(CacheConstants.STATS_HOME_PREFIX + userId)).thenReturn(null);
            when(userMapper.countTodayPractice(eq(userId), any())).thenReturn(0);
            when(userMapper.countTotalPractice(userId)).thenReturn(0);
            when(userMapper.countCorrectPractice(userId)).thenReturn(0);

            HomeStatsVO result = statsService.getHomeStats(userId);

            assertNotNull(result);
            assertEquals(0, result.getTotal());
            assertEquals(0, result.getAccuracy(), "总数为0时正确率应为0，不抛除零异常");
        }
    }

    @Nested
    @DisplayName("AuthService Session 缓存")
    class AuthServiceCacheTest {

        @Mock
        private UserMapper userMapper;

        @Mock
        private RedisUtil redisUtil;

        @Mock
        private JwtUtils jwtUtils;

        @org.mockito.Mock
        private com.shaanxi.zhiping.config.WxMiniAppConfig wxConfig;

        @InjectMocks
        private AuthService authService;

        @Test
        @DisplayName("getCurrentUser 命中缓存：直接返回，不查 DB")
        void getCurrentUser_cacheHit_shouldNotQueryDb() {
            Long userId = 1L;
            User cached = new User();
            cached.setId(userId);
            cached.setNickname("缓存中的用户");
            when(redisUtil.get(CacheConstants.SESSION_USER_PREFIX + userId)).thenReturn(cached);

            User result = authService.getCurrentUser(userId);

            assertNotNull(result);
            assertEquals(userId, result.getId());
            assertEquals("缓存中的用户", result.getNickname());
            verify(userMapper, never()).selectById(anyLong());
            verify(redisUtil, never()).set(anyString(), any(), anyLong());
        }

        @Test
        @DisplayName("getCurrentUser 未命中缓存：查 DB 并回写缓存（TTL=7天）")
        void getCurrentUser_cacheMiss_shouldQueryDbAndWriteCache() {
            Long userId = 2L;
            User dbUser = new User();
            dbUser.setId(userId);
            dbUser.setNickname("DB中的用户");
            when(redisUtil.get(CacheConstants.SESSION_USER_PREFIX + userId)).thenReturn(null);
            when(userMapper.selectById(userId)).thenReturn(dbUser);

            User result = authService.getCurrentUser(userId);

            assertNotNull(result);
            assertEquals("DB中的用户", result.getNickname());
            verify(userMapper, times(1)).selectById(userId);
            verify(redisUtil, times(1)).set(
                    eq(CacheConstants.SESSION_USER_PREFIX + userId),
                    eq(dbUser),
                    eq(CacheConstants.TTL_SESSION));
            // TTL 应为 7 天 = 604800 秒
            assertEquals(604800L, CacheConstants.TTL_SESSION);
        }

        @Test
        @DisplayName("getCurrentUser 未命中且 DB 无此用户：返回 null，不写缓存")
        void getCurrentUser_dbEmpty_shouldNotWriteCache() {
            Long userId = 3L;
            when(redisUtil.get(CacheConstants.SESSION_USER_PREFIX + userId)).thenReturn(null);
            when(userMapper.selectById(userId)).thenReturn(null);

            User result = authService.getCurrentUser(userId);

            assertNull(result);
            verify(redisUtil, never()).set(anyString(), any(), anyLong());
        }

        @Test
        @DisplayName("logout：同时清除 token 缓存和 user 缓存")
        void logout_shouldClearBothTokenAndUserCache() {
            String token = "test-token-abc";
            Long userId = 5L;
            when(jwtUtils.getUserIdFromToken(token)).thenReturn(userId);
            when(redisUtil.delete(CacheConstants.SESSION_TOKEN_PREFIX + token)).thenReturn(true);
            when(redisUtil.delete(CacheConstants.SESSION_USER_PREFIX + userId)).thenReturn(true);

            boolean ok = authService.logout(token);

            assertTrue(ok);
            verify(redisUtil, times(1)).delete(CacheConstants.SESSION_TOKEN_PREFIX + token);
            verify(redisUtil, times(1)).delete(CacheConstants.SESSION_USER_PREFIX + userId);
        }

        @Test
        @DisplayName("logout：token 为空时直接返回 false，不清缓存")
        void logout_emptyToken_shouldReturnFalse() {
            boolean ok = authService.logout("");

            assertFalse(ok);
            verify(redisUtil, never()).delete(anyString());
        }

        @Test
        @DisplayName("logout：token 为 null 时直接返回 false")
        void logout_nullToken_shouldReturnFalse() {
            boolean ok = authService.logout(null);

            assertFalse(ok);
            verify(redisUtil, never()).delete(anyString());
        }

        @Test
        @DisplayName("isSessionValid：Redis 中存在该 token 则有效")
        void isSessionValid_tokenExists_shouldReturnTrue() {
            String token = "valid-token";
            when(redisUtil.hasKey(CacheConstants.SESSION_TOKEN_PREFIX + token)).thenReturn(true);

            boolean valid = authService.isSessionValid(token);

            assertTrue(valid);
        }

        @Test
        @DisplayName("isSessionValid：Redis 中无该 token 则无效")
        void isSessionValid_tokenMissing_shouldReturnFalse() {
            String token = "invalid-token";
            when(redisUtil.hasKey(CacheConstants.SESSION_TOKEN_PREFIX + token)).thenReturn(false);

            boolean valid = authService.isSessionValid(token);

            assertFalse(valid);
        }
    }
}
