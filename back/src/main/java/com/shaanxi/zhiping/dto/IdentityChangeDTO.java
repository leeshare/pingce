package com.shaanxi.zhiping.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class IdentityChangeDTO {

    @NotBlank(message = "修改原因不能为空")
    private String reason;
}