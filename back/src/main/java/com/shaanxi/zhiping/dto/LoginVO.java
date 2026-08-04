package com.shaanxi.zhiping.dto;

import lombok.Data;

/**
 * 登录返回 VO
 */
@Data
public class LoginVO {

    private String token;
    private Long userId;
    private String openid;
    private String nickname;
    private String avatar;
    private Integer memberLevel;
    private Boolean isNewUser;
}
