package com.shaanxi.zhiping.service;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shaanxi.zhiping.common.CacheConstants;
import com.shaanxi.zhiping.dto.AdminLoginDTO;
import com.shaanxi.zhiping.dto.AdminLoginVO;
import com.shaanxi.zhiping.dto.AdminUserVO;
import com.shaanxi.zhiping.entity.AdminUser;
import com.shaanxi.zhiping.exception.BusinessException;
import com.shaanxi.zhiping.mapper.AdminUserMapper;
import com.shaanxi.zhiping.security.JwtUtils;
import com.shaanxi.zhiping.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 管理后台 - 认证服务
 */
@Slf4j
@Service
public class AdminAuthService {

    @Resource
    private AdminUserMapper adminUserMapper;

    @Resource
    private JwtUtils jwtUtils;

    @Resource
    private RedisUtil redisUtil;

    /**
     * 管理员登录
     */
    public AdminLoginVO login(AdminLoginDTO dto) {
        LambdaQueryWrapper<AdminUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AdminUser::getUsername, dto.getUsername());
        AdminUser admin = adminUserMapper.selectOne(wrapper);
        if (admin == null) {
            throw new BusinessException("用户名或密码错误");
        }
        if (admin.getStatus() != null && admin.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }
        if (!BCrypt.checkpw(dto.getPassword(), admin.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        // 更新最后登录时间
        AdminUser update = new AdminUser();
        update.setId(admin.getId());
        update.setLastLoginAt(LocalDateTime.now());
        adminUserMapper.updateById(update);

        String token = jwtUtils.generateAdminToken(admin.getId(), admin.getUsername());

        // 写 Redis Session
        redisUtil.set(CacheConstants.SESSION_ADMIN_TOKEN_PREFIX + token,
                admin.getId(), CacheConstants.TTL_SESSION);
        redisUtil.set(CacheConstants.SESSION_ADMIN_USER_PREFIX + admin.getId(),
                admin, CacheConstants.TTL_SESSION);

        AdminLoginVO vo = new AdminLoginVO();
        vo.setToken(token);
        vo.setUser(toVO(admin));
        log.info("管理员登录成功, adminId={}, username={}", admin.getId(), admin.getUsername());
        return vo;
    }

    /**
     * 获取当前登录管理员信息
     */
    public AdminUserVO getCurrentAdmin(Long adminId) {
        AdminUser admin = loadById(adminId);
        return toVO(admin);
    }

    /**
     * 退出登录
     */
    public boolean logout(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        boolean deleted = redisUtil.delete(CacheConstants.SESSION_ADMIN_TOKEN_PREFIX + token);
        Long adminId = jwtUtils.getAdminIdFromToken(token);
        if (adminId != null) {
            redisUtil.delete(CacheConstants.SESSION_ADMIN_USER_PREFIX + adminId);
        }
        return deleted;
    }

    /**
     * 校验管理员 token Session 是否有效
     */
    public boolean isSessionValid(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        return redisUtil.hasKey(CacheConstants.SESSION_ADMIN_TOKEN_PREFIX + token);
    }

    /**
     * 根据 id 加载管理员（优先 Redis 缓存）
     */
    public AdminUser loadById(Long adminId) {
        String key = CacheConstants.SESSION_ADMIN_USER_PREFIX + adminId;
        AdminUser cached = redisUtil.get(key);
        if (cached != null) {
            return cached;
        }
        AdminUser admin = adminUserMapper.selectById(adminId);
        if (admin == null) {
            throw new BusinessException("管理员不存在");
        }
        redisUtil.set(key, admin, CacheConstants.TTL_SESSION);
        return admin;
    }

    /**
     * 将实体转换为 VO（不返回密码）
     */
    public AdminUserVO toVO(AdminUser admin) {
        AdminUserVO vo = new AdminUserVO();
        vo.setId(admin.getId());
        vo.setUsername(admin.getUsername());
        vo.setNickname(admin.getNickname());
        vo.setAvatar(admin.getAvatar());
        vo.setIsSuper(admin.getIsSuper());
        vo.setStatus(admin.getStatus());
        vo.setLastLoginAt(admin.getLastLoginAt());
        vo.setCreatedAt(admin.getCreatedAt());
        vo.setUpdatedAt(admin.getUpdatedAt());
        if (admin.isSuperAdmin()) {
            vo.setPermissions(Collections.singletonList("*"));
        } else if (admin.getPermissions() == null || admin.getPermissions().isEmpty()) {
            vo.setPermissions(Collections.emptyList());
        } else {
            vo.setPermissions(Arrays.asList(admin.getPermissions().split(",")));
        }
        return vo;
    }

    /**
     * 将权限列表转换为存储格式字符串
     */
    public String joinPermissions(List<String> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return "";
        }
        return String.join(",", permissions);
    }
}
