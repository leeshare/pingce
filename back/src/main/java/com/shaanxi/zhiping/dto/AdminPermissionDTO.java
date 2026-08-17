package com.shaanxi.zhiping.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 批量分配权限 DTO
 */
@Data
public class AdminPermissionDTO {

    @NotEmpty(message = "权限列表不能为空")
    private List<String> permissions;
}
