package com.shaanxi.zhiping.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 学员列表项 VO
 * <p>数据来自 t_user（微信小程序登录时自动注册）+ t_practice_record 聚合统计
 */
@Data
public class StudentVO {

    private Long id;

    /** 昵称（小程序授权） */
    private String nickname;

    /** 头像 */
    private String avatar;

    /** 手机号（已绑定则有） */
    private String phone;

    /** 性别 0未知 1男 2女 */
    private Integer gender;

    /** 身份：高中生/中职生/复读生 等 */
    private String identity;

    /** 省 */
    private String province;

    /** 市 */
    private String city;

    /** 区/县 */
    private String district;

    /** 学校 */
    private String school;

    /** 年级 */
    private String grade;

    /** 目标专业 */
    private String targetMajor;

    /** 会员等级 0普通 1VIP */
    private Integer memberLevel;

    /** 会员到期时间 */
    private LocalDateTime memberExpireTime;

    /** 累计刷题数（来自 t_practice_record） */
    private Integer totalPractice;

    /** 正确率（0~100，整数） */
    private Integer correctRate;

    /** 最近一次刷题时间 */
    private LocalDateTime lastPracticeAt;

    /** 注册时间 */
    private LocalDateTime createdAt;
}
