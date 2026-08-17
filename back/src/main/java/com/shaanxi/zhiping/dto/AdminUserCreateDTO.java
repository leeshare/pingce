package com.shaanxi.zhiping.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 创建/更新管理员 DTO
 */
@Data
public class AdminUserCreateDTO {

    /** 创建时必填，更新时可选（留空表示不修改密码） */
    private String password;

    @NotBlank(message = "用户名不能为空")
    @Size(max = 64, message = "用户名长度不能超过 64")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字、下划线")
    private String username;

    @Size(max = 64, message = "昵称长度不能超过 64")
    private String nickname;

    private String avatar;

    /** 0禁用 1启用 */
    private Integer status;

    /** 权限码，逗号分隔；超管创建时传 * */
    private String permissions;

    /** 是否超管 0/1；仅超管可设置 */
    private Integer isSuper;
}
