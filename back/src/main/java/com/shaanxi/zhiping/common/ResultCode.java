package com.shaanxi.zhiping.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 返回码枚举
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    SUCCESS(200, "成功"),
    FAIL(500, "失败"),

    // 认证相关 4xxx
    UNAUTHORIZED(401, "未登录或token已过期"),
    FORBIDDEN(403, "无权限访问"),
    LOGIN_FAIL(4001, "登录失败"),
    WX_LOGIN_FAIL(4002, "微信登录失败"),
    TOKEN_INVALID(4003, "token无效"),

    // 参数相关 4xxx
    PARAM_ERROR(400, "参数错误"),
    PARAM_NULL(4010, "参数为空"),

    // 业务相关 5xxx
    USER_NOT_FOUND(5001, "用户不存在"),
    USER_EXISTS(5002, "用户已存在"),
    DATA_NOT_FOUND(5004, "数据不存在"),
    ;

    private final Integer code;
    private final String message;
}
