package com.shaanxi.zhiping.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 微信小程序配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "wx.miniapp")
public class WxMiniAppConfig {

    private String appid;
    private String secret;
    private String loginUrl;
}
