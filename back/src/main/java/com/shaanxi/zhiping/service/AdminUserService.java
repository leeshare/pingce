package com.shaanxi.zhiping.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shaanxi.zhiping.common.PageResult;
import com.shaanxi.zhiping.common.CacheConstants;
import com.shaanxi.zhiping.dto.AdminPermissionDTO;
import com.shaanxi.zhiping.dto.AdminPermissionOptionVO;
import com.shaanxi.zhiping.dto.AdminUserCreateDTO;
import com.shaanxi.zhiping.dto.AdminUserQueryDTO;
import com.shaanxi.zhiping.dto.AdminUserVO;
import com.shaanxi.zhiping.entity.AdminUser;
import com.shaanxi.zhiping.exception.BusinessException;
import com.shaanxi.zhiping.mapper.AdminUserMapper;
import com.shaanxi.zhiping.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 管理后台 - 用户管理服务
 */
@Slf4j
@Service
public class AdminUserService {

    @Resource
    private AdminUserMapper adminUserMapper;

    @Resource
    private AdminAuthService adminAuthService;

    @Resource
    private RedisUtil redisUtil;

    /**
     * 分页查询
     */
    public PageResult<AdminUserVO> page(AdminUserQueryDTO dto) {
        Page<AdminUser> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<AdminUser> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(dto.getKeyword())) {
            wrapper.and(w -> w.like(AdminUser::getUsername, dto.getKeyword())
                    .or().like(AdminUser::getNickname, dto.getKeyword()));
        }
        if (dto.getStatus() != null) {
            wrapper.eq(AdminUser::getStatus, dto.getStatus());
        }
        if (dto.getIsSuper() != null) {
            wrapper.eq(AdminUser::getIsSuper, dto.getIsSuper());
        }
        wrapper.orderByDesc(AdminUser::getId);
        IPage<AdminUser> result = adminUserMapper.selectPage(page, wrapper);
        List<AdminUserVO> records = result.getRecords().stream()
                .map(adminAuthService::toVO)
                .collect(Collectors.toList());
        return new PageResult<>(result.getTotal(), (long) dto.getPage(), (long) dto.getSize(), records);
    }

    /**
     * 详情
     */
    public AdminUserVO detail(Long id) {
        return adminAuthService.toVO(loadById(id));
    }

    /**
     * 新增管理员
     */
    @Transactional
    public AdminUserVO create(AdminUserCreateDTO dto, AdminUser operator) {
        // 校验用户名唯一
        LambdaQueryWrapper<AdminUser> existWrapper = new LambdaQueryWrapper<>();
        existWrapper.eq(AdminUser::getUsername, dto.getUsername());
        if (adminUserMapper.selectCount(existWrapper) > 0) {
            throw new BusinessException("用户名已存在");
        }
        if (StrUtil.isBlank(dto.getPassword())) {
            throw new BusinessException("请填写初始密码");
        }
        AdminUser admin = new AdminUser();
        admin.setUsername(dto.getUsername());
        admin.setPassword(BCrypt.hashpw(dto.getPassword()));
        admin.setNickname(dto.getNickname());
        admin.setAvatar(dto.getAvatar());
        admin.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());

        // 仅超管可设置 isSuper
        boolean targetIsSuper = dto.getIsSuper() != null && dto.getIsSuper() == 1;
        if (targetIsSuper) {
            if (operator == null || !operator.isSuperAdmin()) {
                throw new BusinessException("仅超管可创建超管账号");
            }
            admin.setIsSuper(1);
            admin.setPermissions("*");
        } else {
            admin.setIsSuper(0);
            admin.setPermissions(StrUtil.isBlank(dto.getPermissions()) ? "" : dto.getPermissions());
        }
        adminUserMapper.insert(admin);
        log.info("创建管理员成功, id={}, username={}, operator={}",
                admin.getId(), admin.getUsername(), operator == null ? null : operator.getUsername());
        return adminAuthService.toVO(admin);
    }

    /**
     * 更新管理员
     */
    @Transactional
    public AdminUserVO update(Long id, AdminUserCreateDTO dto, AdminUser operator) {
        AdminUser admin = loadById(id);
        if (admin.isSuperAdmin() && (operator == null || !operator.isSuperAdmin())) {
            throw new BusinessException("无权修改超管账号");
        }
        if (StrUtil.isNotBlank(dto.getNickname())) {
            admin.setNickname(dto.getNickname());
        }
        if (dto.getAvatar() != null) {
            admin.setAvatar(dto.getAvatar());
        }
        if (dto.getStatus() != null) {
            admin.setStatus(dto.getStatus());
        }
        if (StrUtil.isNotBlank(dto.getPassword())) {
            admin.setPassword(BCrypt.hashpw(dto.getPassword()));
        }
        // 仅超管可修改权限与超管标识
        if (operator != null && operator.isSuperAdmin() && !admin.isSuperAdmin()) {
            if (dto.getIsSuper() != null) {
                admin.setIsSuper(dto.getIsSuper() == 1 ? 1 : 0);
                if (admin.getIsSuper() == 1) {
                    admin.setPermissions("*");
                }
            }
            if (admin.getIsSuper() == 0 && dto.getPermissions() != null) {
                admin.setPermissions(dto.getPermissions());
            }
        }
        adminUserMapper.updateById(admin);
        invalidateUserCache(admin.getId());
        return adminAuthService.toVO(admin);
    }

    /**
     * 删除（仅普通管理员可被删除；超管不可删除自己或其他超管）
     */
    @Transactional
    public void delete(Long id, AdminUser operator) {
        if (operator == null) {
            throw new BusinessException("未登录");
        }
        if (!operator.isSuperAdmin()) {
            throw new BusinessException("仅超管可删除管理员");
        }
        AdminUser admin = loadById(id);
        if (admin.isSuperAdmin()) {
            throw new BusinessException("超管账号不可删除");
        }
        if (admin.getId().equals(operator.getId())) {
            throw new BusinessException("不可删除当前登录账号");
        }
        adminUserMapper.deleteById(id);
        invalidateUserCache(id);
    }

    /**
     * 批量分配权限
     */
    @Transactional
    public AdminUserVO assignPermissions(Long id, AdminPermissionDTO dto, AdminUser operator) {
        if (operator == null || !operator.isSuperAdmin()) {
            throw new BusinessException("仅超管可分配权限");
        }
        AdminUser admin = loadById(id);
        if (admin.isSuperAdmin()) {
            throw new BusinessException("超管拥有全部权限，无需分配");
        }
        admin.setPermissions(adminAuthService.joinPermissions(dto.getPermissions()));
        adminUserMapper.updateById(admin);
        invalidateUserCache(admin.getId());
        return adminAuthService.toVO(admin);
    }

    /**
     * 内置权限清单
     */
    public List<AdminPermissionOptionVO> permissionOptions() {
        return AdminPermissionOptionVO.BUILTIN;
    }

    private AdminUser loadById(Long id) {
        AdminUser admin = adminUserMapper.selectById(id);
        if (admin == null) {
            throw new BusinessException("管理员不存在");
        }
        return admin;
    }

    private void invalidateUserCache(Long adminId) {
        redisUtil.delete(CacheConstants.SESSION_ADMIN_USER_PREFIX + adminId);
    }
}
