package com.shaanxi.zhiping.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 微信登录请求 DTO
 */
@Data
public class WxLoginDTO {

    @NotBlank(message = "code 不能为空")
    private String code;

    @NotBlank(message = "nickname 不能为空")
    private String nickname;

    private String avatar;

    private Integer gender;
}
