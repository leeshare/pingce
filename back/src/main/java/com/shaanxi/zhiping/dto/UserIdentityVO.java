package com.shaanxi.zhiping.dto;

import lombok.Data;

@Data
public class UserIdentityVO {

    private String identity;

    private String province;

    private String city;

    private String district;

    private String school;
}