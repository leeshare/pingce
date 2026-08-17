package com.shaanxi.zhiping.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理员用户 VO
 */
@Data
public class AdminUserVO {

    private Long id;

    private String username;

    private String nickname;

    private String avatar;

    private Integer isSuper;

    private List<String> permissions;

    private Integer status;

    private LocalDateTime lastLoginAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
