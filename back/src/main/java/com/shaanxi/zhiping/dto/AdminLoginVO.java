package com.shaanxi.zhiping.dto;

import lombok.Data;

/**
 * 管理员登录响应
 */
@Data
public class AdminLoginVO {

    private String token;

    private AdminUserVO user;
}
