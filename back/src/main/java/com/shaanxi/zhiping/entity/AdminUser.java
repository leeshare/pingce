package com.shaanxi.zhiping.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理后台用户实体
 */
@Data
@TableName("t_admin_user")
public class AdminUser {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户名 */
    private String username;

    /** BCrypt 加密密码 */
    private String password;

    /** 昵称 */
    private String nickname;

    /** 头像 */
    private String avatar;

    /** 是否超管 0否 1是 */
    private Integer isSuper;

    /** 权限码，逗号分隔；超管为 * */
    private String permissions;

    /** 0禁用 1启用 */
    private Integer status;

    /** 最后登录时间 */
    private LocalDateTime lastLoginAt;

    @TableField(fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;

    /** 是否超管 */
    public boolean isSuperAdmin() {
        return isSuper != null && isSuper == 1;
    }

    /** 是否拥有指定权限 */
    public boolean hasPermission(String code) {
        if (isSuperAdmin()) {
            return true;
        }
        if (permissions == null || permissions.isEmpty() || code == null) {
            return false;
        }
        for (String p : permissions.split(",")) {
            if (code.equalsIgnoreCase(p.trim())) {
                return true;
            }
        }
        return false;
    }
}
