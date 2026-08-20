package com.shaanxi.zhiping.dto;

import lombok.Data;

/**
 * 学员（小程序用户）查询 DTO
 */
@Data
public class StudentQueryDTO {

    /** 关键字：昵称 / 手机号 */
    private String keyword;

    /** 身份：高中生/中职生/复读生 等 */
    private String identity;

    /** 会员等级 0普通 1VIP null 不限 */
    private Integer memberLevel;

    /** 省份 */
    private String province;

    /** 学校关键字 */
    private String school;

    /** 页码 */
    private Integer page = 1;

    /** 每页数量 */
    private Integer size = 10;
}
