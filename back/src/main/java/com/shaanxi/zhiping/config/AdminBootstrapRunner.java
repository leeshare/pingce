package com.shaanxi.zhiping.config;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shaanxi.zhiping.entity.AdminUser;
import com.shaanxi.zhiping.mapper.AdminUserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 管理后台启动初始化器
 * - 首次启动时若不存在超管账号，则创建默认超管 admin / admin123
 */
@Slf4j
@Component
@Order(10)
public class AdminBootstrapRunner implements CommandLineRunner {

    @Resource
    private AdminUserMapper adminUserMapper;

    @Override
    public void run(String... args) {
        LambdaQueryWrapper<AdminUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AdminUser::getIsSuper, 1).last("LIMIT 1");
        AdminUser exist = adminUserMapper.selectOne(wrapper);
        if (exist != null) {
            log.info("已存在超管账号 username={}，跳过初始化", exist.getUsername());
            return;
        }
        AdminUser superAdmin = new AdminUser();
        superAdmin.setUsername("admin");
        superAdmin.setPassword(BCrypt.hashpw("admin123"));
        superAdmin.setNickname("超级管理员");
        superAdmin.setIsSuper(1);
        superAdmin.setPermissions("*");
        superAdmin.setStatus(1);
        adminUserMapper.insert(superAdmin);
        log.warn("【初始超管账号已创建】 username=admin password=admin123  请及时修改密码！");
    }
}
