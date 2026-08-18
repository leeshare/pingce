package com.shaanxi.zhiping.service;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shaanxi.zhiping.common.CacheConstants;
import com.shaanxi.zhiping.config.WxMiniAppConfig;
import com.shaanxi.zhiping.dto.IdentityChangeDTO;
import com.shaanxi.zhiping.dto.LoginVO;
import com.shaanxi.zhiping.dto.UserIdentityDTO;
import com.shaanxi.zhiping.dto.UserIdentityVO;
import com.shaanxi.zhiping.dto.WxLoginDTO;
import com.shaanxi.zhiping.entity.IdentityChangeRecord;
import com.shaanxi.zhiping.entity.User;
import com.shaanxi.zhiping.mapper.IdentityChangeRecordMapper;
import com.shaanxi.zhiping.mapper.UserMapper;
import com.shaanxi.zhiping.security.JwtUtils;
import com.shaanxi.zhiping.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证服务（微信登录）
 *
 * Session 缓存策略：
 * - 登录成功后，将 token → userId 映射写入 Redis（TTL 7天）
 * - 退出登录时，主动删除 Redis 中的 token（实现立即失效）
 * - JWT 拦截器校验时，除校验签名外，额外校验 Redis 中 token 是否存在
 */
@Slf4j
@Service
public class AuthService {

    @Resource
    private WxMiniAppConfig wxConfig;

    @Resource
    private UserMapper userMapper;

    @Resource
    private IdentityChangeRecordMapper identityChangeRecordMapper;

    @Resource
    private JwtUtils jwtUtils;

    @Resource
    private RedisUtil redisUtil;

    /**
     * 微信小程序登录
     * 1. 通过 code 换取 openid + session_key
     * 2. 查询/创建用户
     * 3. 生成 JWT token
     * 4. 写入 Redis Session
     */
    @Transactional
    public LoginVO wxLogin(WxLoginDTO dto) {
        // 0. 微信 code 防重放检查（同一 code 5分钟内只能用一次）
        String codeKey = CacheConstants.WX_CODE_PREFIX + dto.getCode();
        if (redisUtil.hasKey(codeKey)) {
            log.warn("微信 code 重复使用, code={}", dto.getCode());
            throw new RuntimeException("登录凭证已使用，请重新获取");
        }

        // 1. code2Session 获取 openid
        Map<String, Object> params = new HashMap<>();
        params.put("appid", wxConfig.getAppid());
        params.put("secret", wxConfig.getSecret());
        params.put("js_code", dto.getCode());
        params.put("grant_type", "authorization_code");

        String response = HttpUtil.get(wxConfig.getLoginUrl(), params);
        log.info("微信 code2session 返回: {}", response);

        JSONObject json = JSONUtil.parseObj(response);
        String openid = json.getStr("openid");
        String sessionKey = json.getStr("session_key");

        if (openid == null) {
            String errMsg = json.getStr("errmsg", "微信登录失败");
            Integer errcode = json.getInt("errcode", -1);
            log.error("微信登录失败 errcode={} errmsg={}", errcode, errMsg);
            throw new RuntimeException("微信登录失败: " + errMsg);
        }

        // 标记 code 已使用（防重放）
        redisUtil.set(codeKey, "1", CacheConstants.TTL_WX_CODE);

        // 2. 查询/创建用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getOpenid, openid);
        User user = userMapper.selectOne(wrapper);
        boolean isNewUser = false;

        if (user == null) {
            // 新用户注册
            user = new User();
            user.setOpenid(openid);
            user.setUnionId(json.getStr("unionid"));
            user.setNickname(dto.getNickname());
            user.setAvatar(dto.getAvatar());
            user.setGender(dto.getGender() != null ? dto.getGender() : 0);
            user.setMemberLevel(0);
            userMapper.insert(user);
            isNewUser = true;
            log.info("新用户注册, userId={}, openid={}", user.getId(), openid);
        } else {
            // 更新昵称头像
            user.setNickname(dto.getNickname());
            if (dto.getAvatar() != null) {
                user.setAvatar(dto.getAvatar());
            }
            if (dto.getGender() != null) {
                user.setGender(dto.getGender());
            }
            userMapper.updateById(user);
        }

        // 3. 生成 JWT token
        String token = jwtUtils.generateToken(user.getId(), openid, user.getNickname());

        // 4. 写入 Redis Session
        //    a) token → userId（用于拦截器校验 token 有效性）
        //    b) session:user:{userId} → User 对象（避免每次请求查 DB 获取用户信息）
        redisUtil.set(CacheConstants.SESSION_TOKEN_PREFIX + token,
                user.getId(), CacheConstants.TTL_SESSION);
        redisUtil.set(CacheConstants.SESSION_USER_PREFIX + user.getId(),
                user, CacheConstants.TTL_SESSION);
        log.info("用户登录 Session 已写入 Redis, userId={}", user.getId());

        // 5. 组装返回
        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUserId(user.getId());
        vo.setOpenid(openid);
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setMemberLevel(user.getMemberLevel());
        vo.setIsNewUser(isNewUser);
        return vo;
    }

    /**
     * 获取当前登录用户信息（优先从 Redis 缓存读取）
     * 避免每次请求都查 DB
     */
    public User getCurrentUser(Long userId) {
        String userKey = CacheConstants.SESSION_USER_PREFIX + userId;
        User cached = redisUtil.get(userKey);
        if (cached != null) {
            return cached;
        }
        User user = userMapper.selectById(userId);
        if (user != null) {
            redisUtil.set(userKey, user, CacheConstants.TTL_SESSION);
        }
        return user;
    }

    /**
     * 退出登录
     * 主动删除 Redis 中的 Session，使 token 立即失效
     */
    public boolean logout(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        // 清除 token Session
        boolean deleted = redisUtil.delete(CacheConstants.SESSION_TOKEN_PREFIX + token);
        // 清除用户信息缓存
        Long userId = jwtUtils.getUserIdFromToken(token);
        if (userId != null) {
            redisUtil.delete(CacheConstants.SESSION_USER_PREFIX + userId);
        }
        log.info("用户退出登录, Session 已清除: token={}, userId={}", deleted, userId);
        return deleted;
    }

    /**
     * 校验 token 的 Session 是否有效（Redis 中是否存在）
     * 供 JwtInterceptor 调用
     */
    public boolean isSessionValid(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        return redisUtil.hasKey(CacheConstants.SESSION_TOKEN_PREFIX + token);
    }

    public UserIdentityVO getUserIdentity(Long userId) {
        User user = getCurrentUser(userId);
        if (user == null) {
            return null;
        }
        UserIdentityVO vo = new UserIdentityVO();
        vo.setIdentity(user.getIdentity());
        vo.setProvince(user.getProvince());
        vo.setCity(user.getCity());
        vo.setDistrict(user.getDistrict());
        vo.setSchool(user.getSchool());
        return vo;
    }

    @Transactional
    public void updateUserIdentity(Long userId, UserIdentityDTO dto) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setIdentity(dto.getIdentity());
        user.setProvince(dto.getProvince());
        user.setCity(dto.getCity());
        user.setDistrict(dto.getDistrict());
        user.setSchool(dto.getSchool());
        userMapper.updateById(user);

        redisUtil.set(CacheConstants.SESSION_USER_PREFIX + userId, user, CacheConstants.TTL_SESSION);
        log.info("用户身份已更新, userId={}, identity={}", userId, dto.getIdentity());
    }

    @Transactional
    public void submitIdentityChange(Long userId, IdentityChangeDTO dto) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        IdentityChangeRecord record = new IdentityChangeRecord();
        record.setUserId(userId);
        record.setOriginalIdentity(user.getIdentity());
        record.setOriginalProvince(user.getProvince());
        record.setOriginalCity(user.getCity());
        record.setOriginalDistrict(user.getDistrict());
        record.setOriginalSchool(user.getSchool());
        record.setReason(dto.getReason());
        record.setStatus(0);
        identityChangeRecordMapper.insert(record);

        log.info("用户提交身份修改申请, userId={}, reason={}", userId, dto.getReason());
    }
}
