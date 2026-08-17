package com.shaanxi.zhiping.dto;

import lombok.Data;

/**
 * 管理员用户查询 DTO
 */
@Data
public class AdminUserQueryDTO {

    private String keyword;
    private Integer status;
    private Integer isSuper;
    private Integer page = 1;
    private Integer size = 10;
}
